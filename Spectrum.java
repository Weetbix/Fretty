import ij.*;
import java.util.ArrayList;
import java.util.List;

//Not synchronised (dont think it matters for now)
public class Spectrum
{
	List<Float> values = new ArrayList<Float>();
	
	public Spectrum()
	{

	}

	//Returns the number of samples in the spectrum
	public int getSize()
	{
		return values.size();
	}

	//Removes all samples from the spectrum
	public void clear()
	{
		values.clear();
	}
	
	public void addValue( float val ) 
	{
		values.add( val );
	}

	public float getValue( int index )
	{
		return values.get( index );
	}

	public void loadFromFile( String fileName )
	{

	}

}
