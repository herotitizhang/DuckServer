import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utilities class contains methods needed by different classes.
 * @author Xiaowei Xu, Hanxiao Zhang
 *
 */

public class Utilities {
	
	// convert an object to a byte array
	public static byte[] getByteArray(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	// convert a byte array to an object
	public static Object getObject(byte[] byteArray) {
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
