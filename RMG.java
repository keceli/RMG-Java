////////////////////////////////////////////////////////////////////////////////
//
//	RMG - Reaction Mechanism Generator
//
//	Copyright (c) 2002-2009 Prof. William H. Green (whgreen@mit.edu) and the
//	RMG Team (rmg_dev@mit.edu)
//
//	Permission is hereby granted, free of charge, to any person obtaining a
//	copy of this software and associated documentation files (the "Software"),
//	to deal in the Software without restriction, including without limitation
//	the rights to use, copy, modify, merge, publish, distribute, sublicense,
//	and/or sell copies of the Software, and to permit persons to whom the
//	Software is furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in
//	all copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
//	DEALINGS IN THE SOFTWARE.
//
////////////////////////////////////////////////////////////////////////////////


import java.util.*;
import java.io.*;

import jing.chem.*;
import jing.chemParser.*;
import jing.param.*;
import jing.chemUtil.*;
import jing.rxn.*;
import jing.rxnSys.*;
import jing.mathTool.*;

//----------------------------------------------------------------------------
// MainRMG.java
//----------------------------------------------------------------------------

public class RMG {
	
    //## configuration RMG::RMG
    public static void main(String[] args) {
	
    
 	long tAtInitialization = System.currentTimeMillis();
	Global.tAtInitialization = tAtInitialization;
	
	initializeSystemProperties(args[0]);
    ReactionModelGenerator rmg = new ReactionModelGenerator();
        
    // Generate the model!
    rmg.modelGeneration();
        
    CoreEdgeReactionModel cerm = (CoreEdgeReactionModel)rmg.getReactionModel();
	//Write core species to RMG_Dictionary.txt
	String coreSpecies ="";
	Iterator iter = cerm.getSpecies();
	
	if (Species.useInChI) {
		while (iter.hasNext()){
			int i=1;
			Species spe = (Species) iter.next();
			coreSpecies = coreSpecies + spe.getChemkinName() + " " + spe.getInChI() + "\n"+spe.getChemGraph().toString(i)+"\n\n";
		}
	} else {
		while (iter.hasNext()){
			int i=1;
			Species spe = (Species) iter.next();
			coreSpecies = coreSpecies + spe.getChemkinName() + "\n"+spe.getChemGraph().toString(i)+"\n\n";
		}
	}

	try{
		File rmgDictionary = new File("RMG_Dictionary.txt");
		FileWriter fw = new FileWriter(rmgDictionary);
		fw.write(coreSpecies);
		fw.close();
	}
	catch (IOException e) {
    	System.out.println("Could not write RMG_Dictionary.txt");
    	System.exit(0);
    }
	
	//rs.reduceModel();
	//Write the final model output in a separete Final_Model file
	String finalOutput = "";
	//finalOutput = finalOutput + "\n\\\\\\\\\\\\\\\\\\\\\\\\\\    Final Reaction Model Output    \\\\\\\\\\\\\\\\\\\\\\\\\\";
    //finalOutput = finalOutput +"\n"+ cerm.returnPDepModel(rs.getPresentStatus())+"\n";
	
	//finalOutput = finalOutput + "Model Edge:";
	//finalOutput = finalOutput + cerm.getEdge().getSpeciesNumber()+" ";
	//finalOutput = finalOutput + " Species; ";
	//finalOutput = finalOutput + cerm.getEdge().getReactionNumber()+" ";
	//finalOutput = finalOutput + " Reactions.";

       LinkedList speList = new LinkedList(rmg.getReactionModel().getSpeciesSet());

       //11/2/07 gmagoon: changing to loop over all reaction systems
       LinkedList rsList = rmg.getReactionSystemList();
       for(int i=0; i < rsList.size(); i++){
            ReactionSystem rs = (ReactionSystem)rsList.get(i);
            finalOutput = finalOutput + "Reaction system " + (i+1) + " of " + rsList.size() + "\n";//11/4/07 gmagoon: fixing "off by one" error
            finalOutput = finalOutput + "\\\\\\\\\\\\\\\\\\\\\\\\\\\\    Concentration Profile Output    \\\\\\\\\\\\\\\\\\\\\\\\\\";
            finalOutput = finalOutput +"\n"+ rs.returnConcentrationProfile(speList)+"\n";
            if (rmg.getError()){//svp
                    finalOutput = finalOutput + "\n";
                    finalOutput = finalOutput + "Upper Bounds:"+"\n";
                    finalOutput = finalOutput + rs.printUpperBoundConcentrations(speList)+"\n";
                    finalOutput = finalOutput + "Lower Bounds:"+"\n";
                    finalOutput = finalOutput + rs.printLowerBoundConcentrations(speList)+"\n";
        }
            finalOutput = finalOutput + "\\\\\\\\\\\\\\\\\\\\\\\\\\\\    Mole Fraction Profile Output    \\\\\\\\\\\\\\\\\\\\\\\\\\";
            finalOutput = finalOutput +"\n"+ rs.returnMoleFractionProfile(speList)+"\n";
        finalOutput = finalOutput + rs.printOrderedReactions() + "\n";
        if (rmg.getSensitivity()){//svp
              LinkedList importantSpecies = rmg.getSpeciesList();
                      finalOutput = finalOutput + "Sensitivity Analysis:";
                      finalOutput = finalOutput + rs.printSensitivityCoefficients(speList, importantSpecies)+"\n";
                      finalOutput = finalOutput + "\n";
                      finalOutput = finalOutput + rs.printSensitivityToThermo(speList, importantSpecies)+"\n";
                      finalOutput = finalOutput + "\n";
                      finalOutput = finalOutput + rs.printMostUncertainReactions(speList, importantSpecies)+"\n\n";

            }
            finalOutput = finalOutput + rs.returnReactionFlux() + "\n";
            long end = System.currentTimeMillis();
            double min = (end-tAtInitialization)/1E3/60;
            finalOutput = finalOutput +"Running Time is: " + String.valueOf(min) + " minutes.";


            try{
                    File finalmodel = new File("Final_Model.txt");
                    FileWriter fw = new FileWriter(finalmodel);
                    fw.write(finalOutput);
                    fw.close();
            }
            catch (IOException e) {
            System.out.println("Could not write Final_Model.txt");
            System.exit(0);
            }
       }

    };

	public static void initializeSystemProperties(String inputfile) {
	    File f = new File(".");
	    String dir = f.getAbsolutePath();
		File therfit = new File("therfit");
		therfit.mkdir();
		File chemkin = new File("chemkin");
		chemkin.mkdir();
		//writeThermoFile();
		File Restart = new File("Restart");
		Restart.mkdir();
		File GATPFit = new File("GATPFit");
		GATPFit.mkdir();
		File ODESolver = new File("ODESolver");
		ODESolver.mkdir();
		File fit3p = new File("fit3p");
		fit3p.mkdir();
		File chemdis = new File("chemdis");
		ChemParser.deleteDir(chemdis);
		chemdis.mkdir();
		File fame = new File("fame");
		ChemParser.deleteDir(fame);
		fame.mkdir();
		File frankie = new File("frankie");
		ChemParser.deleteDir(frankie);
		frankie.mkdir();
		File inchi = new File("InChI");
		ChemParser.deleteDir(inchi);
		inchi.mkdir();
		
		
		 String workingDir = System.getenv("RMG");
	     System.setProperty("RMG.workingDirectory", workingDir);
	     System.setProperty("jing.rxnSys.ReactionModelGenerator.conditionFile",inputfile);
		 try {//svp
             String initialConditionFile = System.getProperty("jing.rxnSys.ReactionModelGenerator.conditionFile");
             if (initialConditionFile == null) {
                     System.out.println("undefined system property: jing.rxnSys.ReactionModelGenerator.conditionFile");
                     System.exit(0);
             }

            // Print out RMG header
			System.out.println("");
			System.out.println("################################################################");
			System.out.println("#                                                              #");
			System.out.println("#              RMG - Reaction Mechanism Generator              #");
			System.out.println("#                         Version 3.0                          #");
			System.out.println("#                         5 March 2009                         #");
			System.out.println("#                                                              #");
			System.out.println("#                 http://rmg.sourceforge.net/                  #");
			System.out.println("#                                                              #");
			System.out.println("#  Copyright (c) 2002-2009                                     #");
			System.out.println("#  Prof. William H. Green and the RMG Team:                    #");
			System.out.println("#    Joshua Allen, Dr. Robert Ashcraft, Dr. Gregory Beran,     #");
			System.out.println("#    C. Franklin Goldsmith, Michael Harper, Gregory Magoon,    #");
			System.out.println("#    Dr. David M. Matheu, Sarah Petway, Sumathy Raman,         #");
			System.out.println("#    Sandeep Sharma, Kevin Van Geem, Dr. Jing Song,            #");
			System.out.println("#    Dr. John Wen, Dr. Richard West, Andrew Wong,              #");
			System.out.println("#    Dr. Hsi-Wu Wong, Dr. Paul E. Yelvington, Dr. Joanna Yu    #");
			System.out.println("#                                                              #");
			System.out.println("#  The RMGVE graphical user interface to the RMG database      #");
			System.out.println("#  was written by John Robotham.                               #");
			System.out.println("#                                                              #");
			System.out.println("#  This software package incorporates parts of the following   #");
			System.out.println("#  software packages:                                          #");
			System.out.println("#    DASSL    - Written by Prof. Linda Petzold et al           #");
			System.out.println("#      http://www.cs.ucsb.edu/~cse/software.html               #");
			System.out.println("#    CDK      - Written by Prof. Cristoph Steinbeck et al      #");
			System.out.println("#      http://cdk.sourceforge.net/                             #");
			System.out.println("#    InChI    - Available from IUPAC                           #");
			System.out.println("#      http://www.iupac.org/inchi/                             #");
			System.out.println("#                                                              #");
			System.out.println("#  For more information, including how to properly cite this   #");
			System.out.println("#  program, see http://rmg.sourceforge.net/.                   #");
			System.out.println("#                                                              #");
			System.out.println("################################################################");
			System.out.println("");

            System.out.println("----------------------------------------------------------------------");
        	 System.out.println(" User input:");
             System.out.println("----------------------------------------------------------------------");

             //GJB: Added mini-reader to reprint input file in job output.
             FileReader tmpin = new FileReader(initialConditionFile);
             BufferedReader minireader = new BufferedReader(tmpin);
             try {
                 String inputLine = minireader.readLine();
                 while ( inputLine != null ) {
                	 System.out.println(inputLine);
                	 inputLine = minireader.readLine();
                 }
              }
             catch ( IOException error ) {
                 System.err.println( "Error reading condition file: " + error );
             }
             System.out.println("\n----------------------------------------------------------------------\n");
             minireader.close();
             
             FileReader in = new FileReader(initialConditionFile);
             BufferedReader reader = new BufferedReader(in);
             String line = ChemParser.readMeaningfulLine(reader);
			 //line = ChemParser.readMeaningfulLine(reader);
			 
             if (line.startsWith("Database")){
               StringTokenizer st = new StringTokenizer(line);
               String next = st.nextToken();
               String name = st.nextToken().trim();
               System.setProperty("jing.chem.ChemGraph.forbiddenStructureFile", workingDir + "/databases/"+name+"/forbiddenStructure/ForbiddenStructure.txt");
               System.setProperty("jing.chem.ThermoGAGroupLibrary.pathName", workingDir + "/databases/" + name+"/thermo");
               System.setProperty("jing.rxn.ReactionTemplateLibrary.pathName", workingDir + "/databases/" + name+"/kinetics");
               System.setProperty("jing.rxn.ReactionLibrary.pathName",workingDir + "/databases/" + name + "/kinetics/reactionLibrary");
             }
             line = ChemParser.readMeaningfulLine(reader);
             if (line.startsWith("PrimaryThermoLibrary")){
               StringTokenizer st = new StringTokenizer(line);
               String next = st.nextToken();
               String name = st.nextToken().trim();
               String thermoDirectory = System.getProperty("jing.chem.ThermoGAGroupLibrary.pathName");
               System.setProperty("jing.chem.PrimaryThermoLibrary.pathName", thermoDirectory+"/"+name);
             }
  }
  catch (IOException e) {
     System.err.println("Error in read in reaction system initialization file!");
}
  }

	
	/* this function is not used, has not been maintained, and is now out of date.
	private static void writeThermoFile() {
		// TODO Auto-generated method stub
		String thermoFile = "300.000  1000.000  5000.000 \n";
		thermoFile += "! neon added by pey (20/6/04) - used thermo for Ar\n";
		thermoFile += "Ne                120186Ne  1               G  0300.00   5000.00  1000.00      1\n";
		thermoFile += " 0.02500000E+02 0.00000000E+00 0.00000000E+00 0.00000000E+00 0.00000000E+00    2\n";
		thermoFile += "-0.07453750E+04 0.04366001E+02 0.02500000E+02 0.00000000E+00 0.00000000E+00    3\n";
		thermoFile += " 0.00000000E+00 0.00000000E+00-0.07453750E+04 0.04366001E+02                   4\n";
		thermoFile += "N2                121286N   2               G  0300.00   5000.00  1000.00      1\n";
		thermoFile += " 0.02926640e+02 0.01487977e-01-0.05684761e-05 0.01009704e-08-0.06753351e-13    2\n";
		thermoFile += "-0.09227977e+04 0.05980528e+02 0.03298677e+02 0.01408240e-01-0.03963222e-04    3\n";
		thermoFile += " 0.05641515e-07-0.02444855e-10-0.01020900e+05 0.03950372e+02                   4\n";
		thermoFile += "Ar                120186Ar  1               G  0300.00   5000.00  1000.00      1\n";
		thermoFile += " 0.02500000e+02 0.00000000e+00 0.00000000e+00 0.00000000e+00 0.00000000e+00    2\n";
		thermoFile += "-0.07453750e+04 0.04366001e+02 0.02500000e+02 0.00000000e+00 0.00000000e+00    3\n";
		thermoFile += " 0.00000000e+00 0.00000000e+00-0.07453750e+04 0.04366001e+02                   4\n";
		thermoFile += "end\n";
		try {
			FileWriter fw = new FileWriter("chemkin/therm.dat");
			fw.write(thermoFile);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	};
    */
}
/*********************************************************************
	File Path	: RMG\RMG\MainRMG.java
*********************************************************************/

