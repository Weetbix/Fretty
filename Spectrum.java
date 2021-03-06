import ij.*;
import ij.measure.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

//Not synchronised (dont think it matters for now)
public class Spectrum
{
	List<Float> values = new ArrayList<Float>();
	
	public Spectrum()
	{

	}

	public Spectrum( Spectrum old )
	{
		values = new ArrayList<Float>(old.values);
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

	public void loadFromFile( String fileName ) throws	FileNotFoundException,
								IOException
	{
		clear();

		BufferedReader in = new BufferedReader( new FileReader(fileName) );
		
		String line;
		while( (line = in.readLine()) != null )
		{
			addValue( Float.parseFloat(line) );
		}
			
		in.close();
	}

	public void saveToFile( String fileName ) throws 	FileNotFoundException,
								IOException
	{
		PrintStream ps = new PrintStream( fileName );
				
		for( float val : values )
			ps.println( val );

		ps.close();		
	}

	public void displayInResultsWindow()
	{
		if( values.size() <= 0 ) return;

		ResultsTable window = new ResultsTable();
		window.reset();

		for( float val : values )
		{
			window.incrementCounter();
			window.addValue( "Total Emission Wavelength", val );
		}
		
		window.show( "Spectrum" );
	}

	public void normaliseTo( float quantumYield )
	{
		float total = 0;
		for( float val : values )
		{
			total += val;
		}
		
		for( int i = 0; i < values.size(); i++ )
		{
			values.set(i, (values.get(i) / total) * quantumYield );
		}
	}

	public float[] asArray()
	{
		float[] a = new float[ values.size() ];
		for( int i = 0; i < values.size(); i++ )
			a[i] = values.get( i );

		return a;
	}

}
