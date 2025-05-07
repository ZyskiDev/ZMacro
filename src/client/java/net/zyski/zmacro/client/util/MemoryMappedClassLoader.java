package net.zyski.zmacro.client.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class MemoryMappedClassLoader extends ClassLoader {
    private final byte[] jarData;

    public MemoryMappedClassLoader(byte[] jarData, String jarName, ClassLoader parent) {
        super(parent);
        this.jarData = jarData;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarData))) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().equals(name.replace('.', '/') + ".class")) {
                    byte[] bytes = jarStream.readAllBytes();
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to load class", e);
        }
        throw new ClassNotFoundException(name);
    }
}
