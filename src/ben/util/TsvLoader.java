package ben.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benh on 5/3/15.
 */
public class TsvLoader {

    final int numCols;
    final List<List<String>> data;

    public TsvLoader(final String filename, final int expectedColumns) throws IOException {
        this.numCols = expectedColumns;
        data = new ArrayList<>();
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while((line = br.readLine()) != null){
            if (!line.startsWith("#") && !line.trim().equals("")) {
                final String[] fields = line.split("\t", -1);
                if (fields.length != numCols) {
                    throw new IllegalStateException("a row didn't have [" + numCols + "] columns, had [" + fields.length + "]:\n" + line);
                }
                data.add(Arrays.asList(fields));
            }
        }
        br.close();
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

}
