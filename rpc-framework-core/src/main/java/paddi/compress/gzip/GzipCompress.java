package paddi.compress.gzip;

import paddi.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 11:32:47
 */
public class GzipCompress implements Compress {

    private static final int BUFFER_SIZE = 1024 * 4;

    @Override
    public byte[] compress(byte[] bytes) {
        checkTrue(bytes == null);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        }catch(IOException e) {
            throw new RuntimeException("gzip compress error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        checkTrue(bytes == null);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))){
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n = gzip.read(buffer)) > -1) {
                out.write(bytes, 0, n);
            }
            return out.toByteArray();
        }catch(IOException e) {
            throw new RuntimeException("gzip decompress error", e);
        }
    }

    void checkTrue(boolean condition) {
        if(condition) {
            throw new NullPointerException("bytes is null");
        }
    }
}
