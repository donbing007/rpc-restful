package com.vmsmia.framework.component.rpc.restful.standard.utils;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.tools.JavaFileObject;

/**
 * 用以测试的内存中字节码加载器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 16:08
 * @since 1.8
 */
public class InMemoryClassLoader extends ClassLoader {

    private Map<String, byte[]> classBytes;

    public InMemoryClassLoader() {
        this.classBytes = new HashMap<>();
    }

    public InMemoryClassLoader(Map<String, byte[]> classBytes) {
        this.classBytes = new HashMap<>(classBytes);
    }

    public void registerClass(String fqn, byte[] bytes) {
        classBytes.put(fqn, bytes);
    }

    public void registerClass(JavaFileObject javaFileObject) throws IOException {
        if (javaFileObject.toUri().getPath().endsWith(".class")) {
            try (InputStream in = javaFileObject.openInputStream()) {
                String className = javaFileObject.getName()
                    .replaceAll("^/CLASS_OUTPUT/", "")
                    .replaceAll(".*classes/", "")
                    .replaceAll("/", ".")
                    .replaceAll(".class$", "");
                byte[] byteCode = ByteStreams.toByteArray(in);
                this.registerClass(className, byteCode);
            }
        } else {
            throw new IOException(String.format("%s not is class.", javaFileObject.getName()));
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classBytes.get(name);
        if (bytes == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

}
