package tools.data.input;

/**
 * @author Frz
 */
public interface LittleEndianAccessor {
	byte readByte();
	char readChar();
	short readShort();
	int readInt();
	long readLong();
	void skip (int num);
	byte []read(int num);
	float readFloat();
	double readDouble();
	String readAsciiString(int n);
	String readNullTerminatedAsciiString();
	String readMapleAsciiString();
	long getBytesRead();
	long available();
}
