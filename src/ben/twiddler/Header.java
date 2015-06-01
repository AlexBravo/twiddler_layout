package ben.twiddler;

import java.math.BigInteger;

import static ben.util.Guards.require;
import static ben.util.Data.*;
import static java.util.Arrays.copyOfRange;

/**
 * Created by benh on 5/16/15.
 */
class Header {
    public static final int SIZE = 16;

    public int version = 4;
    public int chordsOffset = 16;
    public int mouseChordsOffset;
    public int stringTableOffset;
    // default values below are from default header
    public int mouseModeTime = 1500;
    public int mouseJumpTime = 383;
    public int mouseNormalSpeed = 3;
    public int mouseJumpStartSpeed = 6;
    public int mouseAcceleration = 10;
    public int keyRepeatDelay = 100;
    public boolean keyRepeatEnabled = true;
    public boolean massStoreAtPowerUp = true;

    public Header() {}

    public static Header parseFrom(final byte[] bytes, int offset) {
        require(offset + 15 < bytes.length);
        final Header result = new Header();
        result.version = readInt(bytes, offset + 0, 1);
        result.chordsOffset = readLSBFirstInt(bytes, offset + 1, 2);
        result.mouseChordsOffset = readLSBFirstInt(bytes, offset + 3, 2);
        result.stringTableOffset = readLSBFirstInt(bytes, offset + 5, 2);
        result.mouseModeTime = readLSBFirstInt(bytes, offset + 7, 2);
        result.mouseJumpTime = readLSBFirstInt(bytes, offset + 9, 2);
        result.mouseNormalSpeed = readInt(bytes, offset + 11, 1);
        result.mouseJumpStartSpeed = readInt(bytes, offset + 12, 1);
        result.mouseAcceleration = readInt(bytes, offset + 13, 1);
        result.keyRepeatDelay = readInt(bytes, offset + 14, 1);
        final BigInteger optionsByte = new BigInteger(1, copyOfRange(bytes, offset + 15, offset + SIZE));
        result.keyRepeatEnabled = optionsByte.testBit(0);
        result.massStoreAtPowerUp = optionsByte.testBit(2);
        return result;
    }

    public byte[] toBytes() {
        final byte[] bytes = new byte[SIZE];
        writeTo(bytes, 0);
        return bytes;
    }

    public void writeTo(final byte[] bytes, int offset) {
        require(offset + 15 < bytes.length);
        writeInt(version, bytes, offset + 0, 1);
        writeLSBFirstInt(chordsOffset, bytes, offset + 1, 2);
        writeLSBFirstInt(mouseChordsOffset, bytes, offset + 3, 2);
        writeLSBFirstInt(stringTableOffset, bytes, offset + 5, 2);
        writeLSBFirstInt(mouseModeTime, bytes, offset + 7, 2);
        writeLSBFirstInt(mouseJumpTime, bytes, offset + 9, 2);
        writeInt(mouseNormalSpeed, bytes, offset + 11, 1);
        writeInt(mouseJumpStartSpeed, bytes, offset + 12, 1);
        writeInt(mouseAcceleration, bytes, offset + 13, 1);
        writeInt(keyRepeatDelay, bytes, offset + 14, 1);
        BigInteger options = BigInteger.ZERO;
        if (keyRepeatEnabled) options = options.setBit(0);
        if (massStoreAtPowerUp) options = options.setBit(2);
        System.arraycopy(options.toByteArray(), 0, bytes, offset + 15, 1);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("version=" + version).append("\n");
        sb.append("chordsOffset=" + chordsOffset).append("\n");
        sb.append("mouseChordsOffset=" + mouseChordsOffset).append("\n");
        sb.append("stringTableOffset=" + stringTableOffset).append("\n");
        sb.append("mouseModeTime=" + mouseModeTime).append("\n");
        sb.append("mouseJumpTime=" + mouseJumpTime).append("\n");
        sb.append("mouseNormalSpeed=" + mouseNormalSpeed).append("\n");
        sb.append("mouseJumpStartSpeed=" + mouseJumpStartSpeed).append("\n");
        sb.append("mouseAcceleration=" + mouseAcceleration).append("\n");
        sb.append("keyRepeatDelay=" + keyRepeatDelay).append("\n");
        sb.append("keyRepeatEnabled=" + keyRepeatEnabled).append("\n");
        sb.append("massStoreAtPowerUp=" + massStoreAtPowerUp).append("\n");
        return sb.toString();
    }
    
    public String toHexString() {
        StringBuilder sb = new StringBuilder();
        sb.append("version=" + Integer.toHexString(version)).append("\n");
        sb.append("chordsOffset=" + Integer.toHexString(chordsOffset)).append("\n");
        sb.append("mouseChordsOffset=" + Integer.toHexString(mouseChordsOffset)).append("\n");
        sb.append("stringTableOffset=" + Integer.toHexString(stringTableOffset)).append("\n");
        sb.append("mouseModeTime=" + Integer.toHexString(mouseModeTime)).append("\n");
        sb.append("mouseJumpTime=" + Integer.toHexString(mouseJumpTime)).append("\n");
        sb.append("mouseNormalSpeed=" + Integer.toHexString(mouseNormalSpeed)).append("\n");
        sb.append("mouseJumpStartSpeed=" + Integer.toHexString(mouseJumpStartSpeed)).append("\n");
        sb.append("mouseAcceleration=" + Integer.toHexString(mouseAcceleration)).append("\n");
        sb.append("keyRepeatDelay=" + Integer.toHexString(keyRepeatDelay)).append("\n");
        sb.append("keyRepeatEnabled=" + keyRepeatEnabled).append("\n");
        sb.append("massStoreAtPowerUp=" + massStoreAtPowerUp).append("\n");
        return sb.toString();
    }

}
