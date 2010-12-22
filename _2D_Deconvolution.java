import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.event.*;
import javax.*;


//The panel at the top of the gui screen...
class FrettyTopPanel extends JPanel
{
	_2D_Deconvolution mainGUI;
	FRETProcessor processor;

	//Padding
	private static final Insets insets = new Insets( 10,10,10,10 );
	public Insets getInsets() { return insets; }

	public FrettyTopPanel( _2D_Deconvolution frettyGUI, FRETProcessor fretp )
	{
		mainGUI = frettyGUI;
		processor = fretp;

		//CROSS EXCITATION check box
		setLayout( new GridLayout( 4, 2 ) );
		add( new JLabel( "Cross Excitation Correction" ) );
		JCheckBox crossCorrection = new JCheckBox();
		crossCorrection.setSelected( true );
		crossCorrection.addActionListener( 
			new ActionListener(){
				public void actionPerformed( ActionEvent actionEvent ){
					AbstractButton b = (AbstractButton) actionEvent.getSource();
					mainGUI.enableCrossExcitationCorrection(b.getModel().isSelected());
					processor.enableCrossExcitationCorrection( b.getModel().isSelected() );
				}
			});
				
				
		add( crossCorrection );

		add( new JLabel("Wavelengths Per Sample  " ) );
		final JSpinner wavelengthsPerSample = new JSpinner( 
			new SpinnerNumberModel( processor.getWavelengthsPerSample(), 1, 9999, 1 ) );
		wavelengthsPerSample.addChangeListener( 
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ) {
					processor.setWavelengthsPerSample( (Integer) wavelengthsPerSample.getValue() );
				}
			});
		add( wavelengthsPerSample );

		add( new JLabel( "Donor quantum yield" ) );
		JSpinner donorQuantumYield = new JSpinner( 
			new SpinnerNumberModel( (double) processor.getDonorQuantumYield(), 0, 1, 0.001 ) );
		donorQuantumYield.addChangeListener(
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ) {
					final JSpinner source = (JSpinner) e.getSource();
					processor.setDonorQuantumYield( ((Double)source.getModel().getValue()).floatValue() );
				}
			});
		add( donorQuantumYield );

		add( new JLabel( "Acceptor quantum yield" ) );
		JSpinner acceptorQuantumYield = new JSpinner( 
			new SpinnerNumberModel( (double) processor.getAcceptorQuantumYield(), 0,1, 0.001 ) );
		acceptorQuantumYield.addChangeListener(
			new ChangeListener(){
				public void stateChanged( ChangeEvent e ){
					final JSpinner source = (JSpinner) e.getSource();
					processor.setAcceptorQuantumYield( ((Double) source.getValue()).floatValue() );
				}
			});
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

	public FrettyReferenceSpectraPanel( final FRETProcessor processor )
	{
		setBorder( new TitledBorder("Reference Spectra") );
		
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) ); 
		SDD =  new FrettySpectraSelector( "SDD" );
		SAD = 	new FrettySpectraSelector( "SAD" );
		SAA = 	new FrettySpectraSelector( "SAA" );

		//set all the callbacks, so the processor knows when a spectrum has changed...
		SDD.setListener( new FrettySpectraSelector.SpectrumChangedEvent(){
			public void onChange(){
				processor.setSDDSpectrum( SDD.getSpectrum() );
			}
		});

		SAD.setListener( new FrettySpectraSelector.SpectrumChangedEvent(){
			public void onChange(){
				processor.setSADSpectrum( SAD.getSpectrum() );
			}
		});

		SAA.setListener( new FrettySpectraSelector.SpectrumChangedEvent(){
			public void onChange(){
				processor.setSAASpectrum( SAA.getSpectrum() );
			}
		});


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
	public interface SpectrumChangedEvent 
	{
		public void onChange();
	}

	JLabel label;
	JButton load = new JButton( "Load" );
	JButton create = new JButton( "Create" );
	JButton save = new JButton( "Save" );
	JButton view = new JButton( "View" );
	JButton clear = new JButton( "Clear" );

	//The actual spectrum object associated with this GUI panel
	Spectrum spectrum;
	SpectrumChangedEvent listener;

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

		setSpectrum( null );
	}

	public void setListener( SpectrumChangedEvent newListener ) 
	{
		listener = newListener;
	}

	public void setEnabled( boolean b ) 
	{
		label.setEnabled( b );
		load.setEnabled( b );
		create.setEnabled( b );

		if( spectrum != null && b == true )
		{
			save.setEnabled( b );
			view.setEnabled( b );
			clear.setEnabled( b );
		} 
		else if( b == false ) 
		{
			save.setEnabled( b );
			view.setEnabled( b );
			clear.setEnabled( b );
		}
	}

	//Sets the spectrum associated with this GUI panel.
	//If the spectrum is set to null then the text will go red, if the spectrum is okay
	//to use the text will go green.
	public void setSpectrum( Spectrum spec )
	{
		spectrum = spec;
		
		//notify the callback if there is one...
		if( listener != null ) listener.onChange();

		if( spectrum == null )
		{
			label.setForeground( Color.red );

			//dont show these buttons without a spectrum
			save.setEnabled( false );
			view.setEnabled( false );
			clear.setEnabled( false );
		}
		else
		{
			//Empty spectrums do us no good...
			if( spectrum.getSize() <= 0 ) setSpectrum( null );

			label.setForeground( Color.green );

			save.setEnabled( true );
			view.setEnabled( true );
			clear.setEnabled( true );
		}
	}

	public Spectrum getSpectrum()
	{
		return spectrum;
	}
}


class FrettyFRETSamplesPanel extends JPanel
{
	//The two stack selector buttons use this class
	class StackSelectorButton extends JButton
	{
		private ImagePlus stack;
		private Color currentColour = Color.red;

		StackSelectorButton( String title ) 
		{
			super( title ); 
			setStack( null );

			//Add a 'clear' button to clear the stack selection
			final JPopupMenu menu = new JPopupMenu();
			JMenuItem clear = new JMenuItem( "Clear" );
			clear.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					setStack( null );
				}
			});
			menu.add( clear );

			//Add a 'show stack' button to bring the stack to the front
			JMenuItem showStack = new JMenuItem( "Show stack" );
			showStack.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					if( stack != null )
						stack.getWindow().show();
				}
			});
			menu.add( showStack );

			addMouseListener( new MouseAdapter(){
				public void mouseReleased( MouseEvent e )
				{
					if( e.isPopupTrigger() )
						menu.show( e.getComponent(), e.getX(), e.getY() );
				}
			});
		}

		@Override
		public void setEnabled( boolean enable )
		{
			//Need to override this otherwise the disabled button has a green/red label even
			//if it is disabled :(

			if( enable == false )
				setForeground( Color.gray );
			else 
				setForeground( currentColour );

			super.setEnabled( enable );
		}

		public void setStack( ImagePlus newstack) 
		{
			//Sets the stack and adjusts the tooltip for the button
			stack = newstack;
				
			if( stack != null )
			{
				setToolTipText( "Set to: " + stack.getTitle() );
				currentColour = Color.green;
				setForeground( currentColour  );
			}
			else
			{
				setToolTipText( "Not set" );
				currentColour = Color.red;
				setForeground( currentColour  );
			}
		}

		public ImagePlus getStack()
		{
			return stack;
		}
	}

	//GUI members
	private StackSelectorButton donorStackButton;
	private StackSelectorButton acceptorStackButton;
	private static final Insets insets = new Insets( 5,5,5,5 );
	public Insets getInsets() { return insets; }

	FRETProcessor processor;

	public FrettyFRETSamplesPanel( final FRETProcessor the_processor )
	{
		processor = the_processor; 

		///// FRET STACKS TAB
		JTabbedPane tabPanel = new JTabbedPane();

		donorStackButton = new StackSelectorButton("Indicate donor excitation stack");
		donorStackButton.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				ImagePlus newstack = WindowManager.getCurrentImage();
				donorStackButton.setStack( newstack );
				processor.setDonorExcitationStack( newstack );
			}
		});

		acceptorStackButton = new StackSelectorButton("Indicate acceptor excitation stack" );
		acceptorStackButton.addActionListener( new ActionListener(){
			public void actionPerformed( ActionEvent e ){
				ImagePlus newstack = WindowManager.getCurrentImage();
				acceptorStackButton.setStack( newstack );
				processor.setAcceptorExcitationStack( newstack );
			}
		});

		JPanel buttonPanel = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets( 5, 5, 5, 5 );
		buttonPanel.setLayout( new GridBagLayout() );

		buttonPanel.add( donorStackButton, c );
		buttonPanel.add( acceptorStackButton, c);

		tabPanel.addTab( "FRET Samples", buttonPanel );

		///// BATCHING TAB
		JPanel p1 = new JPanel();
		p1.add( new JButton("Build Mega Stack") );
			JPanel wildcardPanel = new JPanel();
			wildcardPanel.setLayout( new GridLayout(2,1) );
			wildcardPanel.setBorder( new TitledBorder("Stack names (wildcard)") );
		
			wildcardPanel.add( new JTextField(15) );
			wildcardPanel.add( new JCheckBox("Include megastacks") );
		p1.add( wildcardPanel );

		tabPanel.addTab( "Batching Stacks", p1 );

		add( tabPanel );
	}

	//update the links between the processors stacks and the ones the buttons know about
	public void updateProcessor()
	{
		processor.setDonorExcitationStack( donorStackButton.getStack() );
		processor.setAcceptorExcitationStack( acceptorStackButton.getStack() );
	}

	public void enableCrossExcitationCorrection( boolean yesno )
	{
		acceptorStackButton.setEnabled( yesno );
	}
}

public class _2D_Deconvolution extends PlugInFrame 
{
	FrettyTopPanel 			topPanel;
	FrettyCommonPanel 			commonPanel;
	FrettyReferenceSpectraPanel		referenceSpectraPanel;
	FrettyFRETSamplesPanel 		FRETSamplesPanel;

	//The main class that does all the FRET work when you hit go
	FRETProcessor processor = new FRETProcessor();

	public _2D_Deconvolution () 
	{
		super("2D Deconvolution");

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

		topPanel = new FrettyTopPanel(this, processor);
		commonPanel = new FrettyCommonPanel();
		referenceSpectraPanel = new FrettyReferenceSpectraPanel( processor );
		FRETSamplesPanel = new FrettyFRETSamplesPanel( processor );

		add( topPanel ); 
		add( commonPanel );
		add( referenceSpectraPanel );
		add( FRETSamplesPanel );

		JPanel p = new JPanel();
			//The GO button!
			JButton b = new JButton( "Create FRET image" );
			b.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e ) 
				{
					try
					{
						long start = System.currentTimeMillis();
	
						FRETSamplesPanel.updateProcessor();
						processor.createFRETImage();

						long elapsedMillis = System.currentTimeMillis() - start;
			
						float seconds = elapsedMillis / 1000F;

						IJ.showMessage( "Milliseconds", "=" + elapsedMillis );
					}
					catch( Exception ex ) 
					{
						JOptionPane.showMessageDialog( 	null, 
											ex.getMessage(), 
											"Error",
											JOptionPane.ERROR_MESSAGE );
					}
				}
			});
			p.add( b );

			b = new JButton( "ROI FRET Values" );
			b.addActionListener( new ActionListener(){
				public void actionPerformed( ActionEvent e )
				{
					try
					{
						FRETSamplesPanel.updateProcessor();
						ImagePlus donorStack = processor.getDonorExcitationStack();
				
						if( donorStack == null )
						{
							JOptionPane.showMessageDialog( 	null, 
									"You need to select a donor stack", 
									"Error",
									JOptionPane.ERROR_MESSAGE );
							return;
						}

						//get a list of FRET spectra from the current stack and ROIs
						Spectrum[] spectra = SpectrumGenerator.arrayFromROI( donorStack );
					
						if( spectra == null || spectra.length <= 0 ) return;
						
						//Get the results ready so we can have something to fill the table with...
						double[] evals = new double[spectra.length];
						for( int i = 0; i < spectra.length; i++ )
						{
							evals[i] = processor.findEValue( spectra[i] );
						}

						double average = 0;
						for( double evalue : evals )
						{
							average += evalue;
						}
						average = average / spectra.length;

						//Get a results table ready to fill
						ResultsTable window = new ResultsTable();
						window.reset();
						window.setPrecision( 10 );

						for( int roi_num = 0; roi_num < spectra.length; roi_num++ )
						{
							window.incrementCounter();
							window.addValue( "ROI", roi_num + 1 );
							window.addValue( "E", evals[roi_num] );
							window.addValue( "Mean", average );
						}

						window.show( "E values for all FRET ROIs" );
					}
					catch( Exception ex )
					{
						JOptionPane.showMessageDialog( 	null, 
											ex.getMessage(), 
											"Error",
											JOptionPane.ERROR_MESSAGE );
					}
				}
			});
			p.add( b );

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
