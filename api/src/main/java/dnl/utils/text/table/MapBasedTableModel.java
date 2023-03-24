package dnl.utils.text.table;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A <code>TextTableModel</code> for <code>java.util.Map</code>.
 *
 * @author Daniel Orr
 */
public class MapBasedTableModel extends TextTableModel {

    private final List<String> columnNames;
    private final List<Map<String, Object>> maps;

    public MapBasedTableModel(@NotNull List<Map<String, Object>> maps) {
        this.columnNames = new ArrayList<String>(maps.get(0).keySet());
        this.maps        = maps;
    }

    @Override
    public int getRowCount() {
        return maps.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var m = maps.get(rowIndex);
        var columnName = columnNames.get(columnIndex);
        return m.get(columnName);
    }

    @Override
    public boolean allowNumberingAt(int row) {
        return false;
    }

    @Override
    public boolean addSeparatorAt(int row) {
        return false;
    }

}
