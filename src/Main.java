/**
 * Created by stefan.ilie on 5/3/17.
 */

import com.sun.xml.internal.xsom.impl.parser.ParserContext;
import org.apache.lucene.*;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;



public class Main {


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

    public static void loadFiles(IndexWriter w, Path filesPath)
    throws IOException, TikaException, SAXException{
        if(Files.isDirectory(filesPath)){
            Files.walkFileTree(filesPath, new SimpleFileVisitor<Path>() {
               @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                   try {
                       loadFile(w, file);
                   } catch (Exception ex){

                   }
                   return FileVisitResult.CONTINUE;
               }
            });
        } else {
            loadFile(w, filesPath);
        }
    }


    public static void loadFile(IndexWriter w, Path filePath)
    throws IOException, TikaException, SAXException{

            File f = new File(filePath.toString());

            ContentHandler h = new BodyContentHandler();
            Metadata meta = new Metadata();
            FileInputStream fs = new FileInputStream(f);

            meta.set(Metadata.RESOURCE_NAME_KEY, f.getCanonicalPath());

            Parser p = new AutoDetectParser();
            ParseContext context = new ParseContext();
            p.parse(fs, h, meta, context);


            addDoc(w, h.toString(), filePath.toString());

    }

    public static void main(String[] args)
        throws IOException, TikaException, SAXException, ParseException{

            String filesPath = "Data";

            String indexPath = "index";

            final Path filePathDirectory = Paths.get(filesPath);
//
            Scanner scan = new Scanner(System.in);

            System.out.println("Insert search term: ");
            String searchWord = scan.nextLine();
//
//            Directory dir = FSDirectory.open(Paths.get(indexPath));

            Directory dir = new RAMDirectory();

//            Analyzer analyser = new RomanianAnalyzer();
            Analyzer analyser = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyser);

//            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter w = new IndexWriter(dir, config);

            loadFiles(w, filePathDirectory);

            w.close();

            if (checkSearchWord(searchWord)){
                //Query
                Query q = new QueryParser("parsedString", analyser).parse(parseString(searchWord));

                //Searching
                try {
                    int hitsPerPage = 10;
                    IndexReader reader = DirectoryReader.open(dir);
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
