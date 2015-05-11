package ben.twiddler.data;

import ben.twiddler.enums.KeyboardSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by benh on 5/10/15.
 */
public class CodeInfo {
    public final boolean hasSection;
    public final KeyboardSection section;
    public final String symbol;
    public final boolean isModifiable;
    public final String modifiedSymbol;
    // TODO: notes should be immutable...
    public final List<String> notes;

    CodeInfo(
            final boolean hasSection,
            final KeyboardSection section,
            final String symbol,
            final boolean isModifiable,
            final String modifiedSymbol,
            final Collection<String> notes) {
        this.hasSection = hasSection;
        this.section = section;
        this.symbol = symbol;
        this.isModifiable = isModifiable;
        this.modifiedSymbol = modifiedSymbol;
        this.notes = new ArrayList<>(notes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (hasSection) {
            sb.append(section.toString()).append("-");
        }
        if (isModifiable) {
            sb.append("(");
        }
        sb.append(symbol);
        if (isModifiable) {
            sb.append("|").append(modifiedSymbol);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
