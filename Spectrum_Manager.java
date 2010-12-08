import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import ij.io.*;

import ij.measure.*;

//Generates a spectrum from the current image stack. The spectrum is
//calculated as the total of all the mean values of the ROIs for the current slice.
//Outputs them as a table, for all slices.
//Works on ALL Rois in the ROI manager regardless of whether or not they are selected
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
		Roi[] selections = roi.getRoisAsArray();
		for( int i = 1; i <= imp.getStackSize(); i++ )
		{
			imp.setSliceWithoutUpdate( i );
			double total = 0;
			for( int roi_num = 0; roi_num < roi.getCount(); roi_num++ )
			{
				//For each ROI in the current slice, find the mean value
				imp.setRoi( selections[roi_num], false );
	
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

//The class/callback etc for the Load spectrum button
class LoadSpectrumFromFile implements ActionListener
{
	public void actionPerformed( ActionEvent e )
	{
		OpenDialog od = new OpenDialog("Select a spectrum file", "");

		//If they didnt open a file, quit. 
		if( od.getFileName() == null ) return;

		Spectrum spectrum = new Spectrum();
		spectrum.loadFromFile( od.getDirectory() + od.getFileName() );
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

		Button loadSpectrum = new Button( "Load Spectrum" );
		loadSpectrum.addActionListener( new LoadSpectrumFromFile() );
		add( loadSpectrum );

		pack();
		GUI.center(this);
		setVisible( true );
	}
}
