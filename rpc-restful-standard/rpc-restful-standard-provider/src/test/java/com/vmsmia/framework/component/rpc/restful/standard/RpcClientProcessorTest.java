package com.vmsmia.framework.component.rpc.restful.standard;


import static com.github.javaparser.StaticJavaParser.parse;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.discovery.InMemoryDiscover;
import com.vmsmia.framework.component.rpc.restful.serializer.string.json.Json;
import com.vmsmia.framework.component.rpc.restful.standard.utils.InMemoryClassLoader;
import com.vmsmia.framework.component.rpc.restful.standard.utils.MockStreamSubscriber;
import com.vmsmia.framework.component.rpc.restful.standard.utils.RandomUtils;
import com.vmsmia.framework.component.rpc.restful.stream.StreamSubscriber;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author bin.dong
 * @version 0.1 2024/4/9 17:28
 * @since 1.8
 */
public class RpcClientProcessorTest {

    private MockWebServer mockWebServer;
    private InMemoryDiscover discovery;
    private OkHttpClient okHttpClient;
    private ExecutorService executor;

    private InMemoryClassLoader classLoader;
    private Compiler compiler;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        discovery = InMemoryDiscover.getInstance();
        discovery.register("test", new Endpoint(mockWebServer.getHostName(), mockWebServer.getPort()));

        classLoader = new InMemoryClassLoader();

        executor = Executors.newFixedThreadPool(10);
        Dispatcher dispatcher = new Dispatcher(executor);
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(30);
        okHttpClient = new OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .retryOnConnectionFailure(true)
            .build();

        this.compiler = javac()
            .withProcessors(new RpcClientProcessor())
            .withOptions("-A" + RpcClientProcessor.Configuration.OPTIONS_USE_FIXED_CLASS_NAME + "=true");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
        discovery.reset();
        executor.shutdown();
        mockWebServer = null;
        discovery = null;
        classLoader = null;
        compiler = null;
    }

    @Test
    public void testInterfaceUnSupport() throws Exception {
        URL url = RpcClientProcessorTest.class.getResource("/mock/NoAnnotationInterface.java");
        assertNotNull(url);
        JavaFileObject mockInterface = JavaFileObjects.forResource(url);

        Compilation compilation = compiler.compile(mockInterface);
        assertEquals(Compilation.Status.SUCCESS, compilation.status(), () -> buildError(compilation.errors()));

        List<JavaFileObject> files = compilation.generatedSourceFiles();
        for (JavaFileObject file : files) {
            String code = file.getCharContent(true).toString();

            CompilationUnit cu = parse(code);
            assertEquals(RpcClientProcessor.GENERATION_PACKAGE, cu.getPackageDeclaration().get().getNameAsString());

            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) cu.getTypes().get(0);
            assertFalse(classDeclaration.isInterface(), "The generated class is not an interface.");
            assertTrue(classDeclaration.isPublic(), "The generated class is not public.");
            assertTrue(classDeclaration.isFinal(), "The generated class is not final.");
            assertEquals(1, classDeclaration.getImplementedTypes().size(),
                "The generated class does not implement the expected interface.");
            assertEquals(0, classDeclaration.getExtendedTypes().size(),
                "The generated class should not extend any other class.");
            assertEquals("NoAnnotationInterface", classDeclaration.getImplementedTypes(0).getNameAsString());
            assertEquals("NoAnnotationInterfaceImpl", classDeclaration.getNameAsString());

            List<MethodDeclaration> methods = classDeclaration.getMethods();
            assertEquals(1, methods.size(), "The generated class should have exactly one method.");

            MethodDeclaration method = methods.get(0);
            assertEquals("call", method.getNameAsString());
            assertEquals(1, method.getParameters().size());
            assertEquals("name", method.getParameter(0).getNameAsString());
            assertEquals("String", method.getParameter(0).getTypeAsString());
            assertEquals("String", method.getTypeAsString());
            assertTrue(method.isPublic(), "The generated method is not public.");
            assertTrue(method.isFinal(), "The generated method is not final.");
            assertTrue(method.getAnnotationByClass(Override.class).isPresent(),
                "The generated method does not have the @Override annotation.");

            assertTrue(method.getBody().isPresent(), "The generated method does not have a body.");
            BlockStmt methodBody = method.getBody().get();

            List<Statement> statements = methodBody.getStatements();
            assertEquals(1, statements.size(), "The generated method body should have exactly one statement.");

            Statement statement = statements.get(0);
            assertTrue(statement instanceof ThrowStmt, "The generated method body should throw an exception.");

            assertTrue(() -> {
                ThrowStmt throwStmt = (ThrowStmt) statement;
                return throwStmt.getExpression().toString().startsWith("new UnsupportedOperationException");
            });
        }

        initMemoryClassLoader(compilation);
        Class<?> implClass =
            classLoader.loadClass(RpcClientProcessor.GENERATION_PACKAGE + ".NoAnnotationInterfaceImpl");
        Object instance = getInstance(implClass);
        Method callMethod = implClass.getMethod("call", String.class);
        assertThrows(UnsupportedOperationException.class, () -> {
            try {
                callMethod.invoke(instance, "test");
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            throw new Exception("not found expected exception.");
        });
    }

    @Test
    public void testGetAnnotationInterface() throws Exception {
        URL url = RpcClientProcessorTest.class.getResource("/mock/GetAnnotationInterface.java");
        assertNotNull(url);
        JavaFileObject mockInterface = JavaFileObjects.forResource(url);

        Compilation compilation = compiler.compile(mockInterface);
        assertEquals(Compilation.Status.SUCCESS, compilation.status(), () -> buildError(compilation.errors()));

        initMemoryClassLoader(compilation);

        Data expectedData = new Data("test", 1);
        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(expectedData))
            .addHeader("Content-Type", "text/plain; charset=utf8"));
        Class<?> implClass =
            classLoader.loadClass(RpcClientProcessor.GENERATION_PACKAGE + ".GetAnnotationInterfaceImpl");
        Object instance = getInstance(implClass);
        Method callMethod = implClass.getMethod("call", String.class, String.class, String.class);
        String result = (String) callMethod.invoke(instance, "test", "100", "read");

        assertEquals(Json.serialize(expectedData), result);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test/get/100?type=read", recordedRequest.getPath());
    }

    @Test
    public void testStreamAnnotationInterface() throws Exception {
        URL url = RpcClientProcessorTest.class.getResource("/mock/StreamAnnotationInterface.java");
        assertNotNull(url);
        JavaFileObject mockInterface = JavaFileObjects.forResource(url);

        Compilation compilation = compiler.compile(mockInterface);
        assertEquals(Compilation.Status.SUCCESS, compilation.status(), () -> buildError(compilation.errors()));
        initMemoryClassLoader(compilation);

        String data = RandomUtils.generateRandomString(512, 1024);
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "text/plain; charset=utf8")
            .setChunkedBody(data, 256)
        );

        Class<?> implClass =
            classLoader.loadClass(RpcClientProcessor.GENERATION_PACKAGE + ".StreamAnnotationInterfaceImpl");
        Object instance = getInstance(implClass);
        Method callMethod = implClass.getMethod("call", String.class, Long.TYPE, StreamSubscriber.class);
        MockStreamSubscriber subscriber = new MockStreamSubscriber(128);
        callMethod.invoke(instance, "test", 100, subscriber);

        while (!subscriber.isFinished()) {
            Thread.sleep(100);
        }

        assertTrue(subscriber.isCompleted());
        String readValue = subscriber.getStringValue();
        assertEquals(data.length(), readValue.length());
        assertEquals(data, subscriber.getStringValue());
    }

    private void injectDiscover(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        Field discoverField = clazz.getDeclaredField(RpcClientProcessor.DISCOVER_MEMBER_VARIABLE_NAME);
        discoverField.setAccessible(true);
        discoverField.set(instance, this.discovery);
    }

    private void injectOkHttpClient(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        Field okHttpClientField = clazz.getDeclaredField(RpcClientProcessor.OKHTTPCLIENT_MEMBER_VARIABLE_NAME);
        okHttpClientField.setAccessible(true);
        okHttpClientField.set(instance, this.okHttpClient);
    }

    private String buildError(List<Diagnostic<? extends JavaFileObject>> errors) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> error : errors) {
            String errorInformation = String.format(
                "Error: %s%n Source file: %s%n Line: %d, Column: %d%n Error code: %s%n%n",
                error.getMessage(null),
                error.getSource() != null ? error.getSource().getName() : "Unknown Source",
                error.getLineNumber(),
                error.getColumnNumber(),
                error.getCode()
            );

            errorMessageBuilder.append(errorInformation);
        }
        return errorMessageBuilder.toString();
    }

    private Object getInstance(Class<?> implClass) throws Exception {
        Object instance = implClass.newInstance();
        injectDiscover(instance);
        injectOkHttpClient(instance);
        return instance;
    }

    private void initMemoryClassLoader(Compilation compilation) throws IOException {
        for (JavaFileObject file : compilation.generatedFiles()) {
            if (file.getKind() == JavaFileObject.Kind.CLASS && file.toUri().getPath().endsWith(".class")) {
                this.classLoader.registerClass(file);
            }
        }
    }

    private static class Data {
        private String c1;
        private int c2;

        public Data() {
        }

        public Data(String c1, int c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        public String getC1() {
            return c1;
        }

        public void setC1(String c1) {
            this.c1 = c1;
        }

        public int getC2() {
            return c2;
        }

        public void setC2(int c2) {
            this.c2 = c2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data data = (Data) o;
            return c2 == data.c2 && Objects.equals(c1, data.c1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2);
        }
    }
}