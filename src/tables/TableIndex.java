package tables;

public class TableIndex {
	public int RowIndex;
	public int ColIndex;
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
