import java.util.Arrays;

/** A wrapper for byte arrays with proper equals and hashCode implementations */
public class ByteArrayWrapper {

    private byte[] contents;

    public ByteArrayWrapper(byte[] b) {
        contents = new byte[b.length];
        System.arraycopy(b, 0, contents, 0, b.length);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (getClass() != other.getClass())
            return false;

        ByteArrayWrapper otherWrapper = (ByteArrayWrapper) other;
        return Arrays.equals(contents, otherWrapper.contents);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(contents);
    }
}
