/**
 * Created by stefan.ilie on 5/3/17.
 */

import org.apache.lucene.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

import java.io.IOException;



public class Main {

    private static final String FILENAME = "/Users/stefan.ilie/IdeaProjects/RegasireaInformatiei/ciorba.txt";
    private static final String FILENAME2 = "/Users/stefan.ilie/IdeaProjects/RegasireaInformatiei/ciorba.html";
    private static final String FILENAME3 = "/Users/stefan.ilie/IdeaProjects/RegasireaInformatiei/test2.txt";


    public static String parseString(String searchWord) {
        if (searchWord.matches(".*[ăâșțîĂÂȘȚÎ].*")){
            if (searchWord.contains("ă")){
                searchWord = searchWord.replaceAll("ă", "a");
            }
            if (searchWord.contains("Ă")){
                searchWord = searchWord.replaceAll("Ă", "A");
            }
            if (searchWord.contains("â")){
                searchWord = searchWord.replaceAll("â", "a");
            }
            if (searchWord.contains("Â")){
                searchWord = searchWord.replaceAll("Â", "a");
            }
            if (searchWord.contains("ș")){
                searchWord = searchWord.replaceAll("ș", "s");
            }
            if (searchWord.contains("Ș")){
                searchWord = searchWord.replaceAll("Ș", "s");
            }
            if (searchWord.contains("ț")){
                searchWord = searchWord.replaceAll("ț", "t");
            }
            if (searchWord.contains("Ț")){
                searchWord = searchWord.replaceAll("Ț", "t");
            }
            if (searchWord.contains("î")){
                searchWord = searchWord.replaceAll("î", "i");
            }
            if (searchWord.contains("Î")){
                searchWord = searchWord.replaceAll("Î", "i");
            }
        }
        return searchWord;
    }

    public static boolean checkSearchWord(String searchWord){
        if (Arrays.asList("si", "in", "la", "și", "în", "sa", "să").contains(searchWord)){
            return false;
        }
        return true;
    }


    public static void loadFile(IndexWriter w){

        ArrayList<String> paths = new ArrayList<String>();
        paths.add(FILENAME);
        paths.add(FILENAME2);
        paths.add(FILENAME3);
        for (String path : paths) {

            BufferedReader br = null;
            FileReader fr = null;

            try {

                fr = new FileReader(path);
                br = new BufferedReader(fr);

                String sCurrentLine;

                br = new BufferedReader(new FileReader(path));

                while ((sCurrentLine = br.readLine()) != null) {
                    addDoc(w, sCurrentLine, path);
                }

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (br != null)
                        br.close();

                    if (fr != null)
                        fr.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }

            }
        }
    }

    public static void main(String[] args) throws ParseException {
        Scanner scan = new Scanner(System.in);

        System.out.println("Insert search term: ");

        String searchWord = scan.nextLine();

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        try {

        IndexWriter w = new IndexWriter(index, config);

        loadFile(w);
//
//        addDoc(w, "Lucene in Action Făcâturî");
//        addDoc(w, "Lucene for Dummies");
//        addDoc(w, "Managing Gigabytes");
//        addDoc(w, "The Art of Computer Șcience");
        w.close();

        } catch (IOException e) {
            System.out.println("moloz la tava:\n"+e.getMessage());
        }

        if (checkSearchWord(searchWord)){
            //Query
            Query q = new QueryParser("parsedString", analyzer).parse(parseString(searchWord));

            //Searching
            try {
                int hitsPerPage = 10;
                IndexReader reader = DirectoryReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(q, hitsPerPage);
                ScoreDoc[] hits = docs.scoreDocs;

                System.out.println("\n"+"Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println("\n-------\n"+d.get("path")+"\n" + (i + 1) + ". " +d.get("text"));
                }
            } catch (IOException e) {
                System.out.println("2moloz la tava:\n"+e.getMessage());
            }

        }
    }

    private static void addDoc(IndexWriter w, String text, String filePath) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new TextField("parsedString", parseString(text), Field.Store.YES));
        doc.add(new TextField("path", filePath, Field.Store.YES));
        w.addDocument(doc);
    }
}
