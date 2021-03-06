package com.b5m.scd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Read SCD files with Iterator-like interface.
 * 
 * @author Paolo D'Apice
 */
public final class ScdFileReader implements Closeable {

    private final static Log log = LogFactory.getLog(ScdFileReader.class);

    final static String DOCID_TAG = "<DOCID>";

    private final BufferedReader reader;

    private Document current;
    private Document next;

    /**
     * Create new instance with the given InputStream.
     */
    public ScdFileReader(InputStream is) {
        reader = new BufferedReader(new InputStreamReader(is));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Read the next document.
     * @return true if a new document was read
     * @throws IOException 
     */
    public boolean nextDocument() throws IOException {
        String line = null;
        while (null != (line = reader.readLine())) {
            if (StringUtils.isBlank(line)) continue;
            
            Entry e = Entry.parse(line);
            if (e.tag.equals(DOCID_TAG)) {
                if (next == null) {
                    if (log.isTraceEnabled()) 
                        log.trace("found first document: " + e);

                    next = new Document();
                    next.add(e);

                    continue;
                }

                if (log.isTraceEnabled())
                    log.trace("found new document: " + e);

                current = next;
                
                next = new Document();
                next.add(e);

                return true;
            }

            // trailing entries before docid are discarded
            if (next == null) {
                if (log.isTraceEnabled())
                    log.trace("ignoring trailing entry: " + e.tag);
                
                continue;
            }

            if (log.isTraceEnabled())
                log.trace("add entry: " + e.tag);

             next.add(e);
        }
        
        if (log.isTraceEnabled())
            log.trace("finished to read file");
        
        // still one document to return
        if (next != null) {
            current = next;
            next = null;

            return true;
        }

        return false;
    }

    /**
     * Get the current document.
     * @return The document that was read.
     */
    public Document getCurrentDocument() {
        return current;
    }

}
