package net.zyski.zmacro.client.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import java.util.HashMap;
import java.util.Map;

public class MemoryMappedClassLoader extends ClassLoader implements AutoCloseable {
    private final byte[] jarData;
    private final Map<String, Class<?>> classCache = new HashMap<>();
    private boolean closed = false;

    public MemoryMappedClassLoader(byte[] jarData, String jarName, ClassLoader parent) {
        super(parent);
        this.jarData = jarData;
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

        try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarData))) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);

                    if (className.equals(name)) {
                        byte[] bytes = jarStream.readAllBytes();
                        Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
                        classCache.put(name, clazz);
                        return clazz;
                    }
                }
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class " + name, e);
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public void close() {
        classCache.clear();
        closed = true;
    }
}
