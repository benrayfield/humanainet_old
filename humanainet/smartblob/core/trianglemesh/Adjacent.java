/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package humanainet.smartblob.core.trianglemesh;

public abstract class Adjacent{
	
	//TODO to allow lines to be added as distance constraints between points not in the same triangle,
	//convert these arrays to lists?
	
	/** starts full of nulls. Size is for all adjacent to fit.
	TODO first 2 adjacent corners are in same layer.
	*/
	public final MovCorner adjacentCorners[];
	
	public final MovLine adjacentLines[];
	
	/** starts full of nulls. Size is for all adjacent to fit. */
	public final MovTri adjacentTris[];
	
	/** fills in the adjacentCorners and adjacentTris arrays which start as containing nulls */
	public abstract void connectAdjacent();

	public Adjacent(int adjacentTris, int adjacentLines, int adjacentCorners){
		this.adjacentTris = new MovTri[adjacentTris];
		this.adjacentLines = new MovLine[adjacentLines];
		this.adjacentCorners = new MovCorner[adjacentCorners];
	}

}
