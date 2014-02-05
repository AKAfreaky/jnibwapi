package scbod;

/** Abstract manager class for BWAPI */
public abstract class Manager {
	public void gameStarted(){}
	public void gameUpdate(){}
	public void gameEnded(){}
	public void unitCreate(int unitID){}
	public void unitDestroy(int unitID){}
	public void unitDiscover(int unitID){}
	public void unitEvade(int unitID){}
	public void unitHide(int unitID){}
	public void unitMorph(int unitID){}
	public void unitShow(int unitID){}
}
