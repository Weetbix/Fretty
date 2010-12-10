import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;

import javax.swing.*;
import javax.swing.border.*;

import javax.*;
import javax.swing.plaf.*;

//The panel at the top of the gui screen...
class FrettyTopPanel extends JPanel
{
	Fretty_ mainGUI;

	//Padding
	private static final Insets insets = new Insets( 10,10,10,10 );
	public Insets getInsets() { return insets; }

	public FrettyTopPanel( Fretty_ frettyGUI )
	{
		mainGUI = frettyGUI;

		//CROSS EXCITATION check box
		setLayout( new GridLayout( 4, 2 ) );
		add( new JLabel( "Cross Excitation Correction" ) );
		JCheckBox crossCorrection = new JCheckBox();
		crossCorrection.setSelected( true );
		crossCorrection.addActionListener( 
			new ActionListener(){
				public void actionPerformed( ActionEvent actionEvent ){
					AbstractButton b = (AbstractButton) actionEvent.getSource();
					if( b.getModel().isSelected() )
						mainGUI.enableCrossExcitationCorrection(true);
					else
						mainGUI.enableCrossExcitationCorrection( false );
				}
			});
				
				
		add( crossCorrection );

		add( new JLabel("Wavelengths Per Sample  " ) );
		JSpinner wavelengthsPerSample = new JSpinner(
					new SpinnerNumberModel( 25, 1, 9999, 1 ) );
		add( wavelengthsPerSample );

		add( new JLabel( "Donor quantum yield" ) );
		JSpinner donorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( donorQuantumYield );

		add( new JLabel( "Acceptor quantum yield" ) );
		JSpinner acceptorQuantumYield = new JSpinner( 
					new SpinnerNumberModel( (double)150, 0, 40000, 0.001 ) );
		add( acceptorQuantumYield );
	}
}

class FrettyCommonPanel extends JPanel 
{
	public FrettyCommonPanel()
	{
		setBorder( new TitledBorder("Common Tools") );
		
		//Opens the ROI manager
		JButton roiMan = new JButton( "Open ROI Manager" );
		roiMan.addActionListener( 
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
		add( roiMan );

		//Does background reduction on the current stack
		JButton bgReduction = new JButton( "Background Reduction" );
		bgReduction.addActionListener(
			new ActionListener(){
				public void actionPerformed( ActionEvent e ){
					IJ.run( "Background Reduction" );
				}
			}
		);
		
		add( bgReduction );
	}
}

class FrettyReferenceSpectraPanel extends JPanel
{
	FrettySpectraSelector SDD;
	FrettySpectraSelector SAD;
	FrettySpectraSelector SAA;

	public FrettyReferenceSpectraPanel()
	{
		setBorder( new TitledBorder("Reference Spectra") );
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ); 
		SDD =  new FrettySpectraSelector( "SDD" );
		SAD = 	new FrettySpectraSelector( "SAD" );
		SAA = 	new FrettySpectraSelector( "SAA" );

		add( SDD );
		add( SAD );
		add( SAA );
	}

	//If there is no cross excitation correct, you dont need the SAA spectrum..
	public void enableCrossExcitationCorrection( boolean yesno )
	{
		SAA.setEnabled( yesno );
	}
}

class FrettySpectraSelector extends JPanel
{
	JLabel label;
	JButton load = new JButton( "Load" );
	JButton create = new JButton( "Create" );
	JButton save = new JButton( "Save" );
	JButton view = new JButton( "View" );
	JButton clear = new JButton( "Clear" );

	//The actual spectrum object associated with this GUI panel
	Spectrum spectrum;

	public FrettySpectraSelector( String spectrumName )
	{	
		label = new JLabel( spectrumName );
		label.setForeground( Color.red ); 

		//CREATE BUTTON
		create.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent actionEvent ){
				if( spectrum == null ) 
					setSpectrum( SpectrumGenerator.generateFromROI() );
				else 
				{
					//We dont want to accidentally erase the spectrum they have already
					//added if they click create and there is an error (eg ROI man isnt open)
					Spectrum s = SpectrumGenerator.generateFromROI();
					if( s != null ) 
						setSpectrum( s );
				}
			}
		});

		//LOAD BUTTON
		load.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				OpenDialog od = new OpenDialog("Select a spectrum file", "");
	
				//Steal the focus back from the main imagej window :(
				requestFocus();

				//If they didnt open a file, bail. 
				if( od.getFileName() == null ) return;

				Spectrum loaded_spec = new Spectrum();
				//Try to load the file and report any error messages
				try
				{
					loaded_spec.loadFromFile( od.getDirectory() + od.getFileName() );
					setSpectrum( loaded_spec );
				}
				catch( Exception ex )
				{
					IJ.showMessage( "Couldnt load the spectrum file: " + ex.getMessage() );
					setSpectrum( null );
				}
			}
		});

		//SAVE BUTTON
		save.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				if( spectrum != null )
				{
					SaveDialog sd = new SaveDialog ("Select a spectrum file", "", ".spec" );
	
					//Steal the focus back from the main imagej window :(
					requestFocus();

					//If they didnt open a file, bail. 
					if( sd.getFileName() == null ) return;

					//Try to save the file and report any error messages
					try
					{
						spectrum.saveToFile( sd.getDirectory() + sd.getFileName() );
					}
					catch( Exception ex )
					{
						IJ.showMessage( "Couldnt load the spectrum file: " + ex.getMessage() );
					}
				}
			}
		});

		//VIEW BUTTON
		view.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				if( spectrum != null ) spectrum.displayInResultsWindow();
			}

		});

		//CLEAR BUTTON
		clear.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				setSpectrum( null );
			}
		});

		Insets buttonMargin = new Insets( 2,4,2,4 );
		create.setMargin( 	buttonMargin  );
		load.setMargin( 	buttonMargin );
		save.setMargin( 	buttonMargin );
		view.setMargin( 	buttonMargin );
		clear.setMargin( 	buttonMargin );

		add( label );
		add( create );
		add( load );
		add( save );
		add( view );
		add( clear );
	}

	public void setEnabled( boolean b ) 
	{
		label.setEnabled( b );
		load.setEnabled( b );
		create.setEnabled( b );
		save.setEnabled( b );
		view.setEnabled( b );
		clear.setEnabled( b );
	}

	//Sets the spectrum associated with this GUI panel.
	//If the spectrum is set to null then the text will go red, if the spectrum is okay
	//to use the text will go green.
	public void setSpectrum( Spectrum spec )
	{
		spectrum = spec;
		if( spectrum == null )
			label.setForeground( Color.red );
		else
		{
			//Empty spectrums do us no good...
			if( spectrum.getSize() <= 0 ) setSpectrum( null );

			label.setForeground( Color.green );
		}
	}
}

class FrettyFRETSamplesPanel extends JPanel
{
	private JButton donorStackButton;
	private JButton acceptorStackButton;

	private static final Insets insets = new Insets( 5,5,5,5 );
	public Insets getInsets() { return insets; }

	public FrettyFRETSamplesPanel()
	{
		setBorder( new TitledBorder("FRET Samples") );
;
		setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JPanel p1 = new JPanel();
		p1.setOpaque( false );
		p1.add( new JButton("Build Mega Stack") );
			JPanel wildcardPanel = new JPanel();
			wildcardPanel.setLayout( new GridLayout(2,1) );
			wildcardPanel.setBorder( new TitledBorder("Stack names (wildcard)") );
		
			wildcardPanel.add( new JTextField(15) );
			wildcardPanel.add( new JCheckBox("Include megastacks") );
		p1.add( wildcardPanel );
		add(p1, c);

		donorStackButton = new JButton("Indicate donor excitation stack");
		acceptorStackButton = new JButton("Indicate acceptor excitation stack" );
		add( donorStackButton, c );
		add( acceptorStackButton, c );
	}

	public void enableCrossExcitationCorrection( boolean yesno )
	{
		acceptorStackButton.setEnabled( yesno );
	}
}

public class Fretty_ extends PlugInFrame 
{
	FrettyTopPanel 			topPanel;
	FrettyCommonPanel 			commonPanel;
	FrettyReferenceSpectraPanel		referenceSpectraPanel;
	FrettyFRETSamplesPanel 		FRETSamplesPanel;

	public Fretty_ () 
	{
		super("Fretty");

		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS)) ;

		/////////////////////////////////////////////////////////
		//Setup gui and callbacks
		////////////////////////////////////////////////////////

		//We need swing for the panel borders, so make it look like the OS items
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e ){};

		topPanel = new FrettyTopPanel(this);
		commonPanel = new FrettyCommonPanel();
		referenceSpectraPanel = new FrettyReferenceSpectraPanel();
		FRETSamplesPanel = new FrettyFRETSamplesPanel();

		add( topPanel ); 
		add( commonPanel );
		add( referenceSpectraPanel );
		add( FRETSamplesPanel );

		JPanel p = new JPanel();
			p.add( new JButton( "Create FRET image" ) );
			p.add( new JButton( "ROI FRET Value" ) );

		add( p );

		pack();
		GUI.center(this);
		setVisible( true );
	}

	//Called whenever the checkbox to select this option is changed. 
	public void enableCrossExcitationCorrection( boolean yesno )
	{
		//Inform the appropriate sub panels that we dont want cross excitation
		referenceSpectraPanel.enableCrossExcitationCorrection( yesno );
		FRETSamplesPanel.enableCrossExcitationCorrection( yesno );
	}
}
