package domFiles;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.util.HashMap;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

// DOM
import database.SQLDataBase;
import java.sql.SQLException;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMParsing {

    public static void main(String[] args) {

        if (args.length < 2) {
                System.exit (1);
        }

        switch (args[0])
        {
            case "sync":
                sync(args[1]);
                break;
            case "writeX":
                xmlWrite("/Users/android/Desktop/BDTestTask/" + args[1]);
                break;
            default:
                System.exit (1);
        }

    }


    private static void sync(String pathName){
        try {
            // Get Document Builder Factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            //Document doc = builder.parse(new File("src/domFiles/simple.xml"));
            Document doc = builder.parse(new File(pathName));

            HashMap<Integer,Position> positionSet = new HashMap<>();
            parseToSet(doc, positionSet);

            // sql db connection and sync
            SQLDataBase sqlDataBase = new SQLDataBase();
            Savepoint savepoint = new Savepoint() {
                @Override
                public int getSavepointId() throws SQLException {
                    return 0;
                }

                @Override
                public String getSavepointName() throws SQLException {
                    return null;
                }
            };
            try {
                sqlDataBase.init();
                sqlDataBase.createDB();
                ResultSet rs = sqlDataBase.getDataBase();
                Position posSql;
                sqlDataBase.connectionAutoCommit(false);
                savepoint = sqlDataBase.setSavePoint();
                while (rs.next()) {
                    posSql = new Position(
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                    Position xmlPosition = positionSet.get(posSql.hashCode());
                    if ((xmlPosition != null) && (!xmlPosition.getDescription().equals(posSql.getDescription()))) {
                        sqlDataBase.updateDataBase(xmlPosition, rs.getInt(1));
                        positionSet.remove(posSql.hashCode());
                    } else if ((xmlPosition != null)) {
                        positionSet.remove(posSql.hashCode());
                    } else {
                        sqlDataBase.deleteFromDataBase(rs.getInt(1));
                        positionSet.remove(posSql.hashCode());
                    }
                }

                Iterator it = positionSet.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry pair = (HashMap.Entry) it.next();
                    sqlDataBase.addToDataBase((Position) pair.getValue());
                    it.remove();
                    //throw new SQLException();
                }

                sqlDataBase.executeCommit();
                sqlDataBase.dispose();
            } catch (SQLException e){
                sqlDataBase.rollBack(savepoint);
                sqlDataBase.dispose();
            }

        } catch (ParserConfigurationException e) {
            System.out.println("The underlying parser does not support the requested features.");
        } catch (FactoryConfigurationError e) {
            System.out.println("Error occurred obtaining Document Builder Factory.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // staff parsing and union to set
    private static void parseToSet(Node node, HashMap<Integer,Position>  positionArrayList)  {
        NodeList nodes = node.getChildNodes();
        //ArrayList<Position> positionArrayList = new ArrayList<>();

        if (nodes != null) {
            NodeList children = nodes.item(0).getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                //Position position = new Position();
                if (children.item(i).getNodeType()!= Node.TEXT_NODE) {
                    parseNodeToPosition(children.item(i), positionArrayList);
                }
            }
        }

    }

    //position parsing
    private static void parseNodeToPosition(Node node, HashMap<Integer,Position>  positionArrayList)  {
        NodeList children = node.getChildNodes();
        if (children != null) {
            Position position = new Position();
                    for (int i=0; i<children.getLength(); i++) {
                        if (children.item(i).getNodeType()== Node.TEXT_NODE) continue;
                        parsePositionChild(position,children.item(i));
                    }
            positionArrayList.put(position.hashCode(),position);
                }
    }

    // depcode depjob description parsing
    private static void parsePositionChild(Position position, Node node){

        if (node.getNodeName().equals("DepCode")) {
            NodeList children = node.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType()== Node.TEXT_NODE) {
                    position.setDepCode(children.item(i).getNodeValue());
                }
            }

        } else if (node.getNodeName().equals("DepJob")) {
            NodeList children = node.getChildNodes();
            for (int i=0; i<children.getLength(); i++) {
                if (children.item(i).getNodeType()== Node.TEXT_NODE)
                    position.setDepJob(children.item(i).getNodeValue());
            }


        } else if (node.getNodeName().equals("Description")) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.TEXT_NODE)
                    position.setDescription(children.item(i).getNodeValue());
            }
        }
        //} else System.out.println("не правильное поле");
    }


    private static void xmlWrite(String fileName){

        SQLDataBase sqlDataBase = new SQLDataBase();
        //try(FileWriter writer = new FileWriter(fileName, false))
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(fileName))))
        {
            sqlDataBase.init();
            ResultSet rs = sqlDataBase.getDataBase();

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            writer.write("<staff>\n");

            while (rs.next()) {
                writer.write("  <position>\n");
                writer.write("    <DepCode>" + rs.getString(2) + "</DepCode>\n");
                writer.write("    <DepJob>" + rs.getString(3) + "</DepJob>\n");
                writer.write("    <Description>" + rs.getString(4) + "</Description>\n");
                writer.write("  </position>\n");
            }

            writer.write("</staff>");
            writer.flush();
            sqlDataBase.dispose();
        } catch (SQLException ex) {
            System.out.println("sql  " + ex.getMessage());
            sqlDataBase.dispose();
        } catch(IOException ex){
            System.out.println(ex.getMessage());
            sqlDataBase.dispose();
        }

    }

}