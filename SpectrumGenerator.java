import ij.*;
import ij.process.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.gui.*;
import ij.measure.*;

public class SpectrumGenerator
{
	private static boolean ROIManagerCheck()
	{
		RoiManager roi = RoiManager.getInstance();
		if( roi == null || roi.getCount() <= 0 ) 
		{
			IJ.showMessage( "Error", "You must have the ROI manager open and some region selected" );
			return false;
		}
		return true;
	}

	//Generates and returns a spectrum object from the current stack and ROI selection
	//averages over all the ROIs
	public static Spectrum generateFromROI()
	{
		//Requires the ROI manager open...
		if( !ROIManagerCheck() ) return null;

		RoiManager roi = RoiManager.getInstance();

		Spectrum newSpectrum = new Spectrum();

		ImagePlus imp = IJ.getImage();
		
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

			//Totals all the averages of all the ROIs. This means that it automatically weights
			//between ROIs. This is doable because later we normalise the spectrum anyway...
			newSpectrum.addValue( (float) total );
		}

		return newSpectrum;
	}

	//Returns an array of spectrum objects based on the ROIs. length of the array will
	//be equal to the total number of ROIs in the ROI manager. This method does not
	//average or weigh between ROIs like in generateFromROI
	public static Spectrum[] arrayFromROI()
	{
		//Requires the ROI manager open...
		if( !ROIManagerCheck() ) return null;

		RoiManager roi = RoiManager.getInstance();

		ImagePlus imp = IJ.getImage();
		
		ImageStatistics stats;
		Roi[] selections = roi.getRoisAsArray();
		Spectrum spectra[] = new Spectrum[ roi.getCount() ];
		for( int i = 0; i < spectra.length; i++ )
			spectra[i] = new Spectrum();

		for( int i = 1; i <= imp.getStackSize(); i++ )
		{
			imp.setSliceWithoutUpdate( i );
			for( int roi_num = 0; roi_num < roi.getCount(); roi_num++ )
			{
				//For each ROI in the current slice, add the values to the respected spectra
				imp.setRoi( selections[roi_num], false );
	
				//Maybe slow? Seems to calculate the other stats even though we 
				//only asked it to calculate the mean. 
				stats = imp.getStatistics( Measurements.MEAN );
				spectra[ roi_num ].addValue( (float) stats.mean );
			}
		}

		return spectra;
	}
}
