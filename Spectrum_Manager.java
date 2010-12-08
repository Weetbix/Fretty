import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import ij.measure.*;

//Generates a spectrum from the current image stack. The spectrum is
//calculated as the total of all the mean values of the ROIs for the current slice.
//Outputs them as a table, for all slices.
class SpectrumGenerator implements ActionListener
{
	private RoiManager roi;
	private ImagePlus imp;
	private ResultsTable resultsWindow;

	public boolean ROIManagerCheck()
	{
		roi = RoiManager.getInstance();
		if( roi == null || roi.getCount() <= 0 ) 
		{
			IJ.showMessage( "Error", "You must have the ROI manager open and some region selected" );
			return false;
		}
		return true;
	}

	public void actionPerformed( ActionEvent e )
	{
		//Requires ROI manager
		if( !ROIManagerCheck() ) return;
		imp = IJ.getImage();
		resultsWindow = new ResultsTable();
		resultsWindow.reset(); 

		ImageStatistics stats;
		for( int i = 1; i <= imp.getStackSize(); i++ )
		{
			double total = 0;
			for( int roi_num = 0; roi_num < roi.getCount(); roi_num++ )
			{
				//For each ROI in the current slice, find the mean value
				roi.select( roi_num );
				imp.setSliceWithoutUpdate( i );

				//Maybe slow? Seems to calculate the other stats even though we 
				//only asked it to calculate the mean. 
				stats = imp.getStatistics( Measurements.MEAN );
				total += stats.mean;
			}

			resultsWindow.incrementCounter();
			resultsWindow.addValue( "Total Emission Wavelength", total );
		}

		resultsWindow.show( "Spectrum" );
	}
}

public class Spectrum_Manager extends PlugInFrame {

	public Spectrum_Manager () 
	{
		super("Fretty Spectrum Manager");

		setLayout( new FlowLayout() );

		/////////////////////////////////////////////////////////
		//Setup gui and callbacks
		////////////////////////////////////////////////////////

		Button ROIOpener = new Button( "Open ROI Manager" );
		ROIOpener.addActionListener( 
			new ActionListener(){ 
				public void actionPerformed( ActionEvent e ) {
					if( RoiManager.getInstance() == null ) 
					{
						IJ.run( "ROI Manager..." );
					}
					else
					{
						RoiManager.getInstance().setVisible( true );
					}
				}
			} );
		add( ROIOpener );

		Button meanGenerator = new Button( "Calculate Spectrum" );
		meanGenerator.addActionListener( new SpectrumGenerator() );
		add( meanGenerator );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}
