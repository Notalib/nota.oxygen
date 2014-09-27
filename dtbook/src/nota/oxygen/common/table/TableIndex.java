package nota.oxygen.common.table;

/**
 * Represents a table index, that is the pair RowIndex,ColIndex
 * @author Ole Holst Andersen (oha@nota.nu)
 */
public class TableIndex {
	/**
	 * The row index part of the table index
	 */
	public int RowIndex;
	/**
	 * The column index part of the table index
	 */
	public int ColIndex;
	/**
	 * Constructor initializing the {@link TableIndex} with given row and column indices
	 * @param r
	 * @param c
	 */
	public TableIndex(int r, int c) {
		RowIndex = r;
		ColIndex = c;
	}
	@Override
	public int hashCode() {
		return RowIndex & ColIndex;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TableIndex) {
			TableIndex other = (TableIndex)obj;
			if (other.RowIndex!=RowIndex) return false;
			if (other.ColIndex!=ColIndex) return false;
			return true;
		}
		return false;
	}
	
	

	
}
