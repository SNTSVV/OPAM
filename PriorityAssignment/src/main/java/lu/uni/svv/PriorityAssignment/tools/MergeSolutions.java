package lu.uni.svv.PriorityAssignment.tools;

import javafx.scene.layout.Priority;
import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalProblem;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.FileManager;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public class MergeSolutions {

    public static void main(String[] args) throws Exception {
        // Environment Settings
        Initializer.initLogger();
        Settings.update(args);
        GAWriter.init("", false);

        // load test file names
        for(int runID=1; runID<= Settings.RUN_CNT; runID++) {
            String path = String.format("%s/Run%02d", Settings.BASE_PATH, runID);
            mergePareto(path);
            mergePopulation(path);
            mergeResult(path);
            FileUtils.deleteDirectory(new File(String.format("%s/_pareto", path)));
            FileUtils.deleteDirectory(new File(String.format("%s/_population", path)));
        }
    }

    public static void mergePareto(String _path) throws IOException {
        System.out.print(_path+" Merge population...");
        String folder = String.format("%s/_pareto", _path);
        List<String> fileList = FileManager.listFilesUsingDirectoryStream(folder);
        if (fileList.size()==0) {
            System.out.println("Not found the files");
            return;
        }
        Collections.sort(fileList);

        // load arrivals from  file and save it
        String output = String.format("%s/_best_pareto.list", _path);
        GAWriter writer = new GAWriter(output, Level.FINE, "Index\tPriorityJSON");

        for(int idx=0; idx<fileList.size(); idx++) {
            String name = fileList.get(idx);
            if (!name.startsWith("priorities")) continue;
            int actualIndex = Integer.parseInt(name.substring(11, name.length()-5));

            // read file
            String fullname = folder + "/" +name;
            BufferedReader br = new BufferedReader(new FileReader(fullname));
            String text = br.readLine();
            br.close();

            // write to file
            writer.write(String.format("%d\t", actualIndex));
            writer.write(text);
            writer.write("\n");
        }
        writer.close();
        System.out.println("..Done");
    }

    public static void mergePopulation(String _path) throws IOException {
        System.out.print(_path+" Merge population...");
        String folder = String.format("%s/_population", _path);
        List<String> fileList = FileManager.listFilesUsingDirectoryStream(folder);
        if (fileList.size()==0) {
            System.out.println("Not found the files");
            return;
        }
        Collections.sort(fileList);

        // load arrivals from  file and save it
        String output = String.format("%s/_last_population.list", _path);
        GAWriter writer = new GAWriter(output, Level.FINE, "Index\tPriorityJSON");

        for(int idx=0; idx<fileList.size(); idx++) {
            String name = fileList.get(idx);
            if (!name.startsWith("last")) continue;
            int actualIndex = Integer.parseInt(name.substring(5, name.length()-5));

            // read file
            String fullname = folder + "/" +name;
            BufferedReader br = new BufferedReader(new FileReader(fullname));
            String text = br.readLine();
            br.close();

            // write to file
            writer.write(String.format("%d\t", actualIndex));
            writer.write(text);
            writer.write("\n");
        }
        writer.close();
        System.out.println("..Done");
    }

    public static void mergeResult(String _path) throws IOException {
        System.out.print(_path+" Result file move..");
        String folder = String.format("%s/_pareto", _path);
        List<String> fileList = FileManager.listFilesUsingDirectoryStream(folder);
        if (fileList.size()==0) {
            System.out.println("Not found the files");
            return;
        }

        // load arrivals from  file and save it
        String output = String.format("%s/_result.txt", _path);
        GAWriter writer = new GAWriter(output, Level.FINE, null);

        for(int idx=0; idx<fileList.size(); idx++) {
            String name = fileList.get(idx);
            if (!name.startsWith("result_cycle")) continue;

            // read file
            String fullname = folder + "/" +name;
            BufferedReader br = new BufferedReader(new FileReader(fullname));
            while(true) {
                String text = br.readLine();
                if (text==null) break;
                writer.write(text);
                writer.write("\n");
            }
            br.close();
        }
        writer.close();
        System.out.println("..Done");
    }

}
