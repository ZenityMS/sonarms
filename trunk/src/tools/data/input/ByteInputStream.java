package tools.data.input;

/**
 * Represents an abstract stream of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public interface ByteInputStream {
    int readByte();
    long getBytesRead();
    long available();
}
