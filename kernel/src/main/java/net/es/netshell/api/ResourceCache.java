package net.es.netshell.api;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lomax on 4/1/16.
 */
public class ResourceCache {
    private HashMap<String,Resource> cache = new HashMap<String,Resource>();
    private HashMap<String,Boolean> cacheUsage = new HashMap<String,Boolean>();
    private long writerInterval = 10L * 1000; // 10 seconds
    private long limiterInterval = 10L * 1000; // 10 seconds
    private int maxUsedPercent = 40;
    private Writer writer;
    private Limiter limiter;

    public class Writer implements Runnable {
        private ResourceCache cache;
        private Thread writerThread;

        public Writer(ResourceCache cache) {
            this.cache = cache;
            this.writerThread = new Thread(this);
            this.writerThread.start();
            try {
                // Give the new thread a chance to start.
                Thread.currentThread().sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    cache.sync();
                    Thread.sleep(this.cache.getWriterInterval());
                } catch (Exception e) {
                    System.out.println("Exception " + e.getMessage());
                    Thread.dumpStack();
                    return;
                }
            }
        }
    }

    public class Limiter implements Runnable {
        private ResourceCache cache;
        private Thread limiterThread;

        public Limiter(ResourceCache cache) {
            this.cache = cache;
            this.limiterThread = new Thread(this);
            this.limiterThread.start();
            try {
                // Give the new thread a chance to start.
                Thread.currentThread().sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private int usedPercent() {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long used = maxMemory - freeMemory;
            return (int) (used*100/maxMemory);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int usedPercent = this.usedPercent();
                    if (usedPercent >= this.cache.getMaxUsedPercent()) {
                        // Try first to run the JVM Garbage Collector.
                        Runtime.getRuntime().gc();
                        usedPercent = this.usedPercent();
                        if (usedPercent >= this.cache.getMaxUsedPercent()) {
                            this.cache.prune();
                        }
                    }
                    cache.sync();
                    Thread.sleep(this.cache.getLimiterInterval());
                } catch (Exception e) {
                    System.out.println("Exception " + e.getMessage());
                    Thread.dumpStack();
                    return;
                }
            }
        }
    }

    public ResourceCache() {
        this.writer = new Writer(this);
        this.limiter = new Limiter(this);
    }

    public ResourceCache(long writerInterval,long limiterInterval,int maxUsedPercent) {
        this.writerInterval = writerInterval;
        this.limiterInterval = limiterInterval;
        this.limiter = new Limiter(this);
        this.writer = new Writer(this);
    }

    public HashMap<String, Resource> getCache() {
        return cache;
    }

    public void setCache(HashMap<String, Resource> cache) {
        this.cache = cache;
    }

    public HashMap<String, Boolean> getCacheUsage() {
        return cacheUsage;
    }

    public void setCacheUsage(HashMap<String, Boolean> cacheUsage) {
        this.cacheUsage = cacheUsage;
    }

    public long getWriterInterval() {
        return writerInterval;
    }

    public void setWriterInterval(long writerInterval) {
        this.writerInterval = writerInterval;
    }

    public long getLimiterInterval() {
        return limiterInterval;
    }

    public void setLimiterInterval(long limiterInterval) {
        this.limiterInterval = limiterInterval;
    }

    public int getMaxUsedPercent() {
        return maxUsedPercent;
    }

    public void setMaxUsedPercent(int maxUsedPercent) {
        this.maxUsedPercent = maxUsedPercent;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Limiter getLimiter() {
        return limiter;
    }

    public void setLimiter(Limiter limiter) {
        this.limiter = limiter;
    }

    private void updateDirtyCache(String id, boolean isDirty) {
        if (this.cacheUsage.containsKey(id)) {
            if (isDirty) {
                Boolean dirty = this.cacheUsage.get(id);
                if (!dirty && isDirty) {
                    this.cacheUsage.put(id, true);
                }
            }
        } else {
            this.cacheUsage.put(id, isDirty);
        }
    }

    private Resource cloneObject(Resource resource) {
        // todo: need to implement proxy
        return resource;
    }

    public synchronized Resource getCachedObject(Container container, String name) {
        return this.getCachedObject(container.getOwner(), container.getResourceName(), name);
    }
    public synchronized Resource getCachedObject(String containerOwner, String containerName, String name) {
        String id = containerOwner + "--" + containerName + "--" + name;
        if (this.cache.containsKey(id)) {
            Resource obj = this.cache.get(id);
            this.updateDirtyCache(id, false);
            return cloneObject(obj);
        }
        return null;
    }

    public synchronized void preloadObject(Container container, Resource resource) {
        this.preloadObject(container.getOwner(), container.getResourceName(), resource);
    }

    public synchronized void preloadObject(String containerOwner, String containerName, Resource resource) {
        String id = containerOwner + "--" + containerName+ "--" + resource.getResourceName();
        this.cache.put(id, resource);
        this.updateDirtyCache(id, true);
    }

    public synchronized void cacheObject(Container container, Resource resource) {
        this.cacheObject(container.getOwner(), container.getResourceName(), resource);
    }

    public synchronized void cacheObject(String containerOwner, String containerName, Resource resource) {
        String id = containerOwner + "--" + containerName+ "--" + resource.getResourceName();
        this.cache.put(id, resource);
        this.updateDirtyCache(id, true);
    }

    /**
     * Synchronize the Resource cache.
     * Stores into the database dirty resource. Also prune from the resource cache resource that are not
     * in cacheUsage (i.e. were neither read or written since last sync.
     */
    public synchronized void sync() throws IOException {
        Set<Map.Entry<String, Boolean>> entries = this.cacheUsage.entrySet();

        ArrayList<ResourceAnchor> toSave = new ArrayList<ResourceAnchor>();
        // Filters out the dirty resources
        for (Map.Entry<String, Boolean> entry : entries) {
            String id = entry.getKey();
            String[] ids = id.split("--");
            String owner = ids[0];
            String container = ids[1];
            Resource resource = this.cache.get(id);
            if (entry.getValue()) {
                // Dirty Resource. Save it.
                ResourceAnchor anchor = new ResourceAnchor(owner,container,resource);
                toSave.add(anchor);
            }
        }
        // Save dirty resource in bulk per collection
        PersistentObject.save(toSave);
        // Reset dirty flag
        for (ResourceAnchor anchor : toSave) {
            String id = anchor.getContainerOwner() + "--" + anchor.getContainerName() + "--" + anchor.getResourceName();
            this.cacheUsage.put(id, false);
        }
    }

    public synchronized void flush() throws IOException {
        this.sync();
        this.cache.clear();
        this.cacheUsage.clear();
    }

    /**
     * Remove all entries in the cache that have not been used. If there is none, flush the whole cache
     */
    private synchronized void prune() throws IOException, InstantiationException {
        this.sync();
        Set<Map.Entry<String, Resource>> entries = this.cache.entrySet();
        ArrayList<ResourceAnchor> toRemove = new ArrayList<ResourceAnchor>();

        // Filters out the dirty resources
        for (Map.Entry<String, Resource> entry : entries) {
            String id = entry.getKey();
            String[] ids = id.split("--");
            String owner = ids[0];
            String container = ids[1];
            Resource resource = entry.getValue();
            if (! this.cacheUsage.containsKey(id)) {
                // Never been used.
                ResourceAnchor anchor = new ResourceAnchor(owner,container,resource);
                toRemove.add(anchor);
            }
        }
        if (toRemove.size() > 0) {
            PersistentObject.delete(toRemove);
        } else {
            this.flush();
        }
    }

}
