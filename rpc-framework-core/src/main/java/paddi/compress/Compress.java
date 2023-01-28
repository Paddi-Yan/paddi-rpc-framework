package paddi.compress;

import com.paddi.extension.SPI;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 11:32:25
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
