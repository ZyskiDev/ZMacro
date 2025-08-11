package net.zyski.zmacro.client.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class MemoryMappedClassLoader extends ClassLoader implements AutoCloseable {

    private final byte[] jarData;
    private final Map<String, Class<?>> classCache = new HashMap<>();
    private final Map<String, byte[]> classBytes = new HashMap<>();
    private boolean closed = false;

    public MemoryMappedClassLoader(byte[] jarData, ClassLoader parent) {
        super(parent);
        this.jarData = jarData;
        preloadClasses();
    }

    private void preloadClasses() {
        try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarData))) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    byte[] bytes = jarStream.readAllBytes();
                    classBytes.put(className, bytes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to preload classes from JAR data", e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (closed) {
            throw new ClassNotFoundException("ClassLoader is closed");
        }

        Class<?> cached = classCache.get(name);
        if (cached != null) {
            return cached;
        }

        byte[] bytes = classBytes.get(name);
        if (bytes != null) {
            Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
            classCache.put(name, clazz);
            return clazz;
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public void close() {
        classCache.clear();
        classBytes.clear();
        closed = true;
    }
}
