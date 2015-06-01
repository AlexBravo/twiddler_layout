package ben.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benh on 5/3/15.
 */
public class TsvLoader {

    private final List<List<String>> data;

    private TsvLoader(final List<List<String>> data) {
        this.data = data;
    }

    public int getNumRows(){
        return data.size();
    }

    public String getCell(final int row, final int col){
        return data.get(row).get(col);
    }

    public List<String> getRow(final int row){
        return data.get(row);
    }

    public List<String> getCol(final int col){
        final List<String> result = new ArrayList<>(data.size());
        for(final List<String> row: data){
            result.add(row.get(col));
        }
        return result;
    }

    public static TsvLoader loadFrom(final String filename, final int expectedColumns) throws IOException {
        return loadFrom(new File(filename), expectedColumns);
    }

    public static TsvLoader loadFrom(final File file, final int expectedColumns) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            final TsvLoader result = loadFrom(br, expectedColumns);
            return result;
        } finally {
            br.close();
        }
    }

    public static TsvLoader loadFromText(final String text, final int expectedColumns) throws IOException {
        return loadFrom(new BufferedReader(new StringReader(text)), expectedColumns);
    }

    public static TsvLoader loadFrom(final BufferedReader reader, final int expectedColumns) throws IOException {
        final List<List<String>> data = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null){
            if (!line.startsWith("//") && !line.trim().equals("")) {
                final String[] fields = line.split("\t", -1);
                if (fields.length != expectedColumns) {
                    throw new IllegalStateException("a row didn't have [" + expectedColumns + "] columns, had [" + fields.length + "]:\n" + line);
                }
                data.add(Arrays.asList(fields));
            }
        }
        return new TsvLoader(data);
    }

}
