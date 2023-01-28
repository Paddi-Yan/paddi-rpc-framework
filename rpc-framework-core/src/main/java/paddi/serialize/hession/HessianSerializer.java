package paddi.serialize.hession;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.paddi.exception.SerializeException;
import paddi.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian is a dynamically-typed, binary serialization and Web Services protocol designed for object-oriented transmission.
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 11:11:05
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch(IOException e) {
            throw new SerializeException("Serialization Failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object obj = hessianInput.readObject();
            return clazz.cast(obj);
        }catch(Exception e) {
            throw new SerializeException("Deserialization Failed");
        }
    }
}
