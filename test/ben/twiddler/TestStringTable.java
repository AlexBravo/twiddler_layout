package ben.twiddler;

import ben.twiddler.data.DisambiguatedSymbolKeyCodes;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benh on 6/5/15.
 */
public class TestStringTable {

    private static StringTable getTestTable(){
        final StringTable st = new StringTable();

        final List<ModifiedKeyCode> mkcs1 = new ArrayList<>();
        final ModifiedKeyCode T = new ModifiedKeyCode(
                EnumSet.of(KeyCodeModifier.LSHFT), DisambiguatedSymbolKeyCodes.getKeyCode("T"));
        final ModifiedKeyCode h = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("h"));
        final ModifiedKeyCode e = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("e"));
        final ModifiedKeyCode spc = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("SPC"));
        mkcs1.add(T);
        mkcs1.add(h);
        mkcs1.add(e);
        mkcs1.add(spc);
        final KeyCodeSequence kcs1 = new KeyCodeSequence(mkcs1);
        st.add(kcs1);

        final List<ModifiedKeyCode> mkcs2 = new ArrayList<>();
        final ModifiedKeyCode lsb = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("["));
        final ModifiedKeyCode rsb = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("]"));
        final ModifiedKeyCode al = new ModifiedKeyCode(DisambiguatedSymbolKeyCodes.getKeyCode("ArrowLeft"));
        mkcs2.add(lsb);
        mkcs2.add(rsb);
        mkcs2.add(al);
        final KeyCodeSequence kcs2 = new KeyCodeSequence(mkcs2);
        st.add(kcs2);

        return st;
    }

    @Test
    public void testToBinaryAndBack(){
        final StringTable st1 = getTestTable();
        final byte[] stBytes = st1.toBytes();
        final StringTable st2 = StringTable.parseFrom(stBytes, 0);

        System.out.println(st1);
        System.out.println(st2);

        assertEquals(st1, st2);
    }

}
