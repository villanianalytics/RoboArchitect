package com.acceleratetechnology.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.acceleratetechnology.controller.exceptions.MissedParameterException;

import lombok.Cleanup;

public class HierarchyCommand extends AbstractCommand{

    /**
     * System logger.
     */
    private final Logger logger = Logger.getLogger(HierarchyCommand.class);
    /**
     * Source csv file command line parameter.
     */
    private static final String SRC_FILE_PARAM = "/srcFile";
    /**
     * Destination csv file command line parameter.
     */
    private static final String DEST_FILE_PARAM = "/destFile";
    /**
     * Convert type pc for parent-child and lb for level based.
     */
    private static final String CONVERT_TYPE_PARAM = "/convertType";
    /**
     * CSV source file delimiter command line parameter.
     */
    private static final String DELIM_SRC_PARAM = "/srcDelim";
    /**
     * CSV destination file delimiter command line parameter.
     */
    private static final String DELIM_DEST_PARAM = "/destDelim";
    /**
     * headerFlag command line parameter.
     */
    private static final String HEADER_FLAG_PARAM = "/headerFlag";
    /**
     * customHeader command line parameter.
     */
    private static final String CUSTOM_HEADER_PARAM = "/customHeader";
    /**
     * parentColIndex command line parameter.
     */
    private static final String COLUMN_INDEX_PARENT_PARAM = "/parentColIndex";
    /**
     * childColIndexs command line parameter.
     */
    private static final String COLUMN_INDEX_CHILD_PARAM = "/childColIndex";
    /**
     * totalAttrib command line parameter.
     */
    private static final String TOTAL_ATTRIBUTES_PARAM = "/totalAttrib";
    /**
     * Delimiter DEFAULT value.
     */
    private static final String DEFAULT_DELIM = ",";
    /**
     * headerFlag DEFAULT value.
     */
    private static final String DEFAULT_HEADER_FLAG = "Y";
    /**
     * convertType DEFAULT value
     */
    private static final String DEFAULT_CONVERT_TYPE = "lb";
    /**
     * parentColIndex DEFAULT value
     */
    private static final String DEFAULT_PARENT_INDEX = "1";
    /**
     * childColIndex DEFAULT value
     */
    private static final String DEFAULT_CHILD_INDEX = "0";

    @Command("-hierarchy")
    public HierarchyCommand(String[] args) throws IOException, MissedParameterException {
        super(args);
    }

    @Override
    public void execute() throws Exception {
        String srcFile = getRequiredAttribute(SRC_FILE_PARAM);	// required
        String destFile = getRequiredAttribute(DEST_FILE_PARAM);// required

        char srcDelim = getDefaultAttribute(DELIM_SRC_PARAM, DEFAULT_DELIM).charAt(0);
        char destDelim = getDefaultAttribute(DELIM_DEST_PARAM, DEFAULT_DELIM).charAt(0);
        String customHeader=getDefaultAttribute(CUSTOM_HEADER_PARAM, "");
        char hFlag=getDefaultAttribute(HEADER_FLAG_PARAM,DEFAULT_HEADER_FLAG).toLowerCase().charAt(0);
        String convertType = getDefaultAttribute(CONVERT_TYPE_PARAM,DEFAULT_CONVERT_TYPE);
        String parentColIndex=getDefaultAttribute(COLUMN_INDEX_PARENT_PARAM,DEFAULT_PARENT_INDEX);
        String childColIndex=getDefaultAttribute(COLUMN_INDEX_CHILD_PARAM,DEFAULT_CHILD_INDEX);
        String tAttrib=getDefaultAttribute(TOTAL_ATTRIBUTES_PARAM,"0");

        String extSrc = FilenameUtils.getExtension(srcFile);
        String extDest = FilenameUtils.getExtension(destFile);

        if(!(extSrc.equals("csv") || extSrc.equals("xlsx") || extSrc.equals("txt")))
        {
            throw new MissedParameterException(extSrc + " is not supported. Supported files extension for source file need to be csv, txt or xlsx");
        }
        if(!(extDest.equals("csv") || extDest.equals("xlsx") || extDest.equals("txt")))
        {
            throw new MissedParameterException(extSrc + " is not supported. Supported files extension fot destination file need to be csv, txt or xlsx");
        }
        if(!(convertType.equals("pc") || convertType.equals("lb") ))
        {
            throw new MissedParameterException(convertType + " is not supported. Supported convert type need to be pc for parant-chile OR lb for level-based");
        }

        boolean headerFlag=true;
        if(hFlag=='n') headerFlag=false;
        else if(hFlag=='y') headerFlag=true;
        else
        {
            throw new MissedParameterException(hFlag + " is not supported. Supported value for headerFlag need to be y or n.");
        }
        srcFile=FilenameUtils.getFullPath(srcFile)+ FilenameUtils.getBaseName(srcFile)+"."+extSrc;
        destFile=FilenameUtils.getFullPath(destFile)+ FilenameUtils.getBaseName(destFile)+"."+extDest;
        int pIndex=Integer.parseInt(parentColIndex);
        int cIndex=Integer.parseInt(childColIndex);
        if(pIndex==cIndex || cIndex<0 || pIndex<0)
        {
            throw new MissedParameterException(pIndex +" and "+cIndex+" are not valid values for parent and child indices respectively");
        }

        int totalAttribs=Integer.parseInt(tAttrib);

        if(convertType.equals("lb"))
            convertParentChildToLevelBased(srcFile,destFile,srcDelim,destDelim,extSrc,extDest,headerFlag,customHeader,pIndex,cIndex);
        else if(convertType.equals("pc"))
        {
            if(totalAttribs<=0)
            {
                throw new MissedParameterException(totalAttribs +" value for totalAttrib is invalid. It's a required attribute for Level based to parent-child based conversion");
            }
            convertLevelBasedToParentChild(srcFile,destFile,srcDelim,destDelim,extSrc,extDest,headerFlag,customHeader,totalAttribs);
        }
    }

    /**
     * This method converts xls/csv/txt file from level based to parent-child hierarchy.
     *
     * @param srcFile			Source file in xlsx,csv or txt
     * @param destFile			Destination file in xlsx, csv ot txt
     * @param srcDelim			Source file delimiter
     * @param destDelim			Destination file delimiter
     * @param extSrc			Extension of source file
     * @param extDest			Extension of destination file
     * @param headerFlag		header flag i.e true means first row is header
     * @param customHeader		custom header in comma separated values
     * @param totalAttribs		Total attributes in output parent-child file
     * @throws IOException
     * @throws ArrayIndexOutOfBoundsException
     */
    private void convertLevelBasedToParentChild(String srcFile, String destFile, char srcDelim, char destDelim,
                                                String extSrc, String extDest, boolean headerFlag, String customHeader, int totalAttribs) throws IOException,ArrayIndexOutOfBoundsException {
        logger.debug("Reading level-based file "+srcFile);
        ArrayList<String> lines=new ArrayList<String>();
        if(extSrc.equals("csv"))
            lines=readCSV(srcFile);
        else
            throw new IOException("Source file must be in csv format");
        logger.debug("Converting to parent-child file "+destFile);
        ParentChildRow headerRow=new ParentChildRow();
        ArrayList<ParentChildRow> rows=new ArrayList<HierarchyCommand.ParentChildRow>();
        int totalAttr=totalAttribs-1; // removing one for parent
        for(int i=0;i<lines.size();i++)
        {
            int pIndex=0;
            String[] attribs=lines.get(i).split(srcDelim+"(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)",-1);
            String lastParent=attribs[pIndex].trim();

            if(i==0 && headerFlag)
            {
                if(customHeader.equals(""))
                {
                    String name=attribs[pIndex+1].trim();
                    name=name.substring(name.indexOf("_")+1);
                    headerRow=new ParentChildRow(name,"Parent");
                    for(int u=2;u<=totalAttr;u++)
                    {
                        String attr=attribs[u].trim();
                        attr=attr.substring(attr.indexOf("_")+1);
                        headerRow.add(attr);
                    }
                }
                else
                {
                    String[] cols=customHeader.split(",",-1);
                    if(cols.length!=(totalAttr+1))
                        throw new ArrayIndexOutOfBoundsException("No of Custom header columns names MUST be equal to number of total attributes");
                    headerRow=new ParentChildRow(cols[1],cols[0]);
                    for(int u=2;u<cols.length;u++)
                    {
                        String attr=cols[u].trim();
                        headerRow.add(attr);
                    }
                }
                continue;
            }

            for(int j=1;j<attribs.length;j=j+totalAttr)
            {
                String child=attribs[j].trim();
                ParentChildRow row=new ParentChildRow(child,lastParent);
                for(int k=j+1;k<j+totalAttr;k++)
                {
                    if(k>=attribs.length)
                        throw new ArrayIndexOutOfBoundsException("You might have passed invalid/incorrect number of total columns in OUTPUT parent-child file");
                    row.add(attribs[k].trim());
                }
                rows.add(row);
                lastParent=child;
            }
        }

        rows.add(0,headerRow);
        // generating output
        ArrayList<String> doneList=new ArrayList<String>();
        ArrayList<String> outputLines=new ArrayList<String>();
        for(int x=0;x<rows.size();x++)
        {
            boolean isDone=false;
            for(int m=0;m<doneList.size();m++)
            {
                if((rows.get(x).getName()+rows.get(x).getParent()).equals(doneList.get(m))) isDone=true;
            }
            if(isDone) continue;
            if(!headerFlag && x==0)
            {
                doneList.add(rows.get(x).getName()+rows.get(x).getParent());
                continue;
            }
            StringBuilder sb=new StringBuilder();
            sb.append(rows.get(x).getName()+destDelim+rows.get(x).getParent());
//			System.out.print(rows.get(x).getName()+destDelim+rows.get(x).getParent());
            for(int y=0;y<rows.get(x).getAttribs().size();y++)
            {
                sb.append(destDelim+rows.get(x).getAttribs().get(y));
//				System.out.print(destDelim+rows.get(x).getAttribs().get(y));
            }
            doneList.add(rows.get(x).getName()+rows.get(x).getParent());
            outputLines.add(sb.toString());
//			System.out.println();
        }
        // saving output

//		 for (int i = 0; i < outputLines.size(); i++)
//		 {
//			 System.out.println(outputLines.get(i));
//		 }

        if(extDest.equals("csv") || extDest.equals("txt") )
            saveCSV(outputLines,destFile);
        else throw new IOException("Only csv or txt files are supported");

    }

    /**
     * This method converts xls/csv/txt file from parent-child to level based hierarchy.
     *
     * @param srcFile			Source file in xlsx,csv or txt
     * @param destFile			Destination file in xlsx, csv ot txt
     * @param srcDelim			Source file delimiter
     * @param destDelim			Destination file delimiter
     * @param extSrc			Extension of source file
     * @param extDest			Extension of destination file
     * @param headerFlag		header flag i.e true means first row is header
     * @param customHeader		custom header in comma separated values
     * @param pIndex			index of parent column default is 1
     * @param cIndex			index of  child/name column default is 0
     * @throws IOException		throws if source file is incorrect
     */
    private void convertParentChildToLevelBased(String srcFile, String destFile, char srcDelim, char destDelim,
                                                String extSrc, String extDest, boolean headerFlag, String customHeader, int pIndex,int cIndex) throws IOException {
        ArrayList<String> lines=new ArrayList<String>();
        logger.debug("Reading parent-child file "+srcFile);
        if(extSrc.equals("csv"))
            lines=readCSV(srcFile);
        else
            throw new IOException("Source file must be in csv format");
        logger.debug("Converting to level-based file "+destFile);
        ParentChildRow headerRow=new ParentChildRow();
        ArrayList<ParentChildRow> rows=new ArrayList<HierarchyCommand.ParentChildRow>();
        for(int i=0;i<lines.size();i++)
        {
            String[] attribs=lines.get(i).split(srcDelim+"(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)",-1);
            String name=attribs[cIndex].trim();
            String parent=attribs[pIndex].trim();
            ParentChildRow row=new ParentChildRow(name, parent);
//			System.out.println(i+"="+attribs.length);
            for(int x=0;x<attribs.length;x++)
            {
                if(!(x==pIndex || x==cIndex))
                {
                    row.add(attribs[x].trim());
                }
            }
            if(headerFlag && i==0)
                headerRow=row;
            else
                rows.add(row);
        }
        // finding root
        String root=null;
        // output
        ArrayList<String> outputLines=new ArrayList<String>();
        //
        int maxLevel=0;
        ArrayList<Integer> doneList=new ArrayList<Integer>();
        for(int i=0;i<rows.size();i++)
        {
            boolean isDone=false;
            for(int m=0;m<doneList.size();m++)
            {
                if(i==doneList.get(m)) isDone=true;
            }
            if(isDone) continue;
            ArrayList<ParentChildRow> upRows=new ArrayList<HierarchyCommand.ParentChildRow>();
            ArrayList<ParentChildRow> downRows=new ArrayList<HierarchyCommand.ParentChildRow>();

            ParentChildRow currentRow=rows.get(i);
            ParentChildRow rootRow=currentRow;

            String name=currentRow.getName();
            String parent=currentRow.getParent();
            // traversing root
            boolean rootFound=false;
            while(!rootFound)
            {
                int rIndex=getParent(rows, parent);
                if(rIndex!=-1)
                {
                    upRows.add(rows.get(rIndex));
                    parent=rows.get(rIndex).getParent();
                    doneList.add(rIndex);
                    rootFound=false;
                }
                else
                {
                    rootFound=true;
                }
            }
            if(upRows.size()==0 && root==null) root=currentRow.parent;
            // traversing child
            boolean childFound=false;
            while(!childFound)
            {
                int rIndex=getChild(rows, name);
                if(rIndex!=-1)
                {
                    downRows.add(rows.get(rIndex));
                    name=rows.get(rIndex).getName();
                    doneList.add(rIndex);
                    childFound=false;
                }
                else
                {
                    childFound=true;
                }
            }

            //System.out.println(currentRow.getName()+" up="+upRows.size()+", Down="+downRows.size());

            ArrayList<ParentChildRow> allRows=new ArrayList<HierarchyCommand.ParentChildRow>();
            boolean addBetween=false;
            if(upRows.size()==0)
            {
                allRows.add(rootRow);
            }
            else if(downRows.size()==0)
            {
                downRows.add(rootRow);
            }
            else
            {
                addBetween=true;
            }
            for( int a=upRows.size()-1;a>=0;a--)
            {
                allRows.add(upRows.get(a));
            }

            if(addBetween)
                allRows.add(rootRow);
            for( int b=0;b<downRows.size();b++)
            {
                allRows.add(downRows.get(b));
            }

            if(allRows.size()!=0)
            {



                ArrayList<String> singleLine=new ArrayList<String>();

                for(int j=0;j<allRows.size();j++)
                {
                    StringBuilder sb1=new StringBuilder();
                    if(j==0)
                    {
                        sb1.append(root);
                        sb1.append(destDelim);
                    }
                    sb1.append(allRows.get(j).getName());
                    sb1.append(destDelim);
                    for(int y=0;y<allRows.get(j).getAttribs().size();y++)
                    {
                        sb1.append(allRows.get(j).getAttribs().get(y));
                        if(y<allRows.get(j).getAttribs().size()-1)
                            sb1.append(destDelim);
                    }
                    singleLine.add(sb1.toString());
                }
                //
                if(maxLevel<singleLine.size())
                    maxLevel=singleLine.size();
                StringBuilder sb3=new StringBuilder();
                for(int z=0;z<singleLine.size();z++)
                {
                    sb3.append(singleLine.get(z));
                    if(z<singleLine.size()-1)
                        sb3.append(destDelim);
                }
                outputLines.add(sb3.toString());
            }
        }
        // rearranging header
        if(headerFlag)
        {
            StringBuilder sb=new StringBuilder();
            sb.append("Top");
            for(int l=1;l<=maxLevel;l++)
            {
                if(customHeader.equals(""))
                {
                    sb.append(destDelim);
                    String name=headerRow.getName().trim();
                    sb.append("L"+l+"_"+name);
                    for(int m=0;m<headerRow.getAttribs().size();m++)
                    {
                        sb.append(destDelim);
                        sb.append("L"+l+"_"+headerRow.getAttribs().get(m).trim());
                    }
                }
                else
                {
                    String[] cols=customHeader.split(",");
                    sb.append(destDelim);
                    sb.append("L"+l+"_"+cols[0].trim());
                    for(int m=2;m<cols.length;m++)
                    {
                        sb.append(destDelim);
                        sb.append("L"+l+"_"+cols[m].trim());
                    }
                }
            }
            outputLines.add(0,sb.toString());
        }



        if(extDest.equals("csv") || extDest.equals("txt") )
            saveCSV(outputLines,destFile);
        else throw new IOException("Only csv or txt files are supported");
    }


    /**
     * Save csv file from lines arraylist and file name
     *
     * @param outputLines		String arraylist of lines
     * @param destFile			name of the destination file
     * @throws IOException
     */
    private void saveCSV(ArrayList<String> outputLines, String destFile) throws IOException {
        FileWriter fw = new FileWriter(destFile);

        PrintWriter pw = new PrintWriter(fw);
        logger.debug("Starting writing to file.");
        for(int i=0;i<outputLines.size();i++)
        {
            pw.println(outputLines.get(i));
        }
        pw.flush();
        pw.close();
        fw.close();
        logger.debug("Done.");
        System.out.println("Contents are converted specified hierarchy successfully.");
    }

    /**
     * Reading a csv file and returning the arraylist of lines
     *
     * @param srcFile				Source csv file
     * @return ArrayList<String>	lines in the csv
     * @throws IOException
     */
    private ArrayList<String> readCSV(String srcFile) throws IOException
    {
        ArrayList<String> lines=new ArrayList<String>();
        Path path = Paths.get(srcFile);
        @Cleanup Scanner fileReader = new Scanner(path.toFile());
        String currentLine;
        while (fileReader.hasNext())
        {
            currentLine = fileReader.nextLine();
            if(!currentLine.isEmpty())
                lines.add(currentLine);
        }
        fileReader.close();
        return lines;
    }


    /**
     *
     * An inner class to represent the nodes/rows in hierarchy
     *
     */
    private class ParentChildRow{
        private String name;
        private String parent;
        private ArrayList<String> attribs;

        public ParentChildRow() {
            this.name="";
            this.parent="";
            this.attribs=new ArrayList<String>();
        }

        public ParentChildRow(String name, String parent) {
            super();
            this.name = name;
            this.parent = parent;
            this.attribs=new ArrayList<String>();
        }

        public void add(String attrib)
        {
            this.attribs.add(attrib);
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getParent() {
            return parent;
        }
        public void setParent(String parent) {
            this.parent = parent;
        }
        public ArrayList<String> getAttribs() {
            return attribs;
        }
        public void setAttribs(ArrayList<String> attribs) {
            this.attribs = attribs;
        }

    }

    /**
     * Getting parent node of current node from list of nodes
     *
     * @param list 		An arrayList of ParentChildRow inner class containing all nodes
     * @param parent		Name of the current node whose parent node is to be found
     * @return	int		-1 if current node has no parent-node, index of the node in the list otherwise
     */
    private int getParent(ArrayList<ParentChildRow> list,String parent)
    {
        int index=-1;
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).getName().equals(parent))
            {
                index=i;
                break;
            }
        }
        return index;
    }

    /**
     * Getting child node of current node from list of nodes
     *
     * @param list 		An arrayList of ParentChildRow inner class containing all nodes
     * @param child		Name of the current node whose child node is to be found
     * @return	int		-1 if current node has no child-node, index of the node in the list otherwise
     */
    private int getChild(ArrayList<ParentChildRow> list,String child)
    {
        int index=-1;
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).getParent().equals(child))
            {
                index=i;
                break;
            }
        }
        return index;
    }


}
