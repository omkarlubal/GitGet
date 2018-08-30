import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class GitGet{
    private static final Scanner in = new Scanner(System.in);
    private static boolean INIT_FLAG=false;

    private static int main_repos_index=0;
    private static String REPO_NAME=null;
    private static String directory_name=null;

    public static void main(String args[]){
        int choice=0;
        String input_url;

        while(choice!=2) {
            System.out.println("Enter or paste a path you want to download to: ");
            directory_name=in.next();
            System.out.print("Enter your URL:");
            input_url=in.next();
            start_engine(input_url,"");
            download_files(input_url);
            System.out.println("Enter: ");
            System.out.println("1 to continue");
            System.out.println("2 to Exit");
            choice=in.nextInt();
        }
        System.out.println("Goodbye!");
    }

    private static void start_engine(String input_url,String p_name){                                           //FILE TREE CREATION


        try {
            Document doc = Jsoup.connect(input_url).get();
            for (Element table : doc.select("tbody")) {
                for(Element row:table.select("tr[class=js-navigation-item]")){
                    Elements column = row.select("td");
                    Elements a_tag = column.select("a");
                    String curr_tag = a_tag.attr("href");

                    if (curr_tag.matches(".*\\btree\\b.*")) {
                        create_dir("https://github.com"+curr_tag,p_name);                                        //DOWNLOAD IF TREE
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void download_files(String input_url) {                                              //DOWNLOAD INITIATION

        try {
            Document doc = Jsoup.connect(input_url).get();
            for (Element table : doc.select("tbody")) {
                for(Element row:table.select("tr[class=js-navigation-item]")){
                    Elements column = row.select("td");
                    Elements a_tag = column.select("a");
                    String curr_tag = a_tag.attr("href");

                    if (curr_tag.matches(".*\\btree\\b.*")) {
                        download_files("https://github.com"+curr_tag);
                    }

                    if (curr_tag.matches(".*\\bblob\\b.*")) {
                        download_stuff("https://github.com"+curr_tag);                                    // DOWNLOAD IF BLOB
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private static void download_stuff(String input_url){
        String user_url;
        URL website = null;
        ReadableByteChannel rbc = null;
        String save_path = null;
        int slash_index=0;

        String extension=null;
        String name=null;
        user_url = input_url;

        if (user_url.matches(".*\\bblob\\b.*")) {
            user_url = user_url.replaceAll("\\bblob\\b", "raw");
        }

        char url[] = user_url.toCharArray();

        for(int i = url.length-1; i>=0; i--){

            if(url[i]=='/'){
                name=user_url.substring(i+1,user_url.length());                            //IF FILE WITHOUT EXTENSION
                extension="";
                break;
            }

            if(url[i]=='.'){
                int dot_index = i;
                for(int j = dot_index+1; j<user_url.length(); j++){                             // GETTING THE EXTENSION
                    if(extension==null){
                        extension=Character.toString(url[j]);
                    }else{
                        extension=extension+Character.toString(url[j]);
                    }
                }

                for(int j = dot_index-1; j>=0; j--){                                          //GETTING THE NAME OF THE FILE
                    if(url[j]=='/'){
                        name=user_url.substring(j+1,dot_index);
                        break;
                    }
                }
                break;
            }

        }

        System.out.println("Dowloading...");
        try {
            website = new URL(user_url);                                                                //CREATE URL
        } catch (Exception e) {
            System.out.println("ERROR: Please check your URL again!");
        }

        if (website != null) {
            try {
                rbc = Channels.newChannel(website.openStream());                                          //CONNECT TO URL
            } catch (Exception e) {
                System.out.println("ERROR: Please check your internet connection!");
            }
        }

        int path_index=input_url.lastIndexOf(REPO_NAME);
        save_path = input_url.substring(path_index+REPO_NAME.length(),input_url.length());

        System.out.println("File name : "+name+"."+extension);
        System.out.println("save path : "+save_path);

        if (rbc != null) {
            try {
                FileOutputStream fos;
                if(name.equals("")){
                    slash_index=save_path.lastIndexOf(".");
                    save_path=save_path.substring(0,slash_index-1);
                    fos = new FileOutputStream(directory_name+"/"+save_path+extension);                          //FOR NO NAME AND ONLY EXTENSION
                }else {
                    fos = new FileOutputStream(directory_name+"/"+save_path);         //NORMAL DOWNLOAD
                }


                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                System.out.println("Successful! \n");
                rbc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void create_dir(String input_url,String path){                                           //CREATE DIRECTORY
        String name=null;

        char url[] = input_url.toCharArray();

        if(path.equals("")) {

            if(REPO_NAME!=null){

                int path_index=input_url.lastIndexOf(REPO_NAME);
                name = input_url.substring(path_index+REPO_NAME.length(),input_url.length());

            }else{
                for (int i = url.length - 1; i >= 0; i--) {

                    if (url[i] == '/') {
                        name = input_url.substring(i + 1, url.length);
                        break;
                    }
                }

                if(!INIT_FLAG){
                    main_repos_index=input_url.indexOf(name);

                    for (int i = main_repos_index - 2; i >= 0; i--) {

                        if (url[i] == '/') {
                            REPO_NAME = input_url.substring(i + 1, main_repos_index-1);
                            System.out.println("REPO NAME: "+REPO_NAME);
                            break;
                        }
                    }

                    INIT_FLAG=true;
                }
            }

        }else{
            int path_index=input_url.lastIndexOf(path);
            name = input_url.substring(path_index+path.length(),input_url.length());

        }

        start_engine(input_url,REPO_NAME);

        System.out.println("Directory created: "+directory_name+"/"+name);

        new File(directory_name+"/" + name).mkdirs();

    }

}