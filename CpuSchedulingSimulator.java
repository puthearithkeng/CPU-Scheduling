import java.util.*;

class Process {
    String id;
    int arrivalTime, burstTime, remainingTime, waitingTime, turnaroundTime, completionTime;
    
    Process(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

public class CpuSchedulingSimulator {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nCPU Scheduling Simulator");
            System.out.println("1. First-Come, First-Served (FCFS)");
            System.out.println("2. Shortest-Job-First (SJF)");
            System.out.println("3. Shortest-Remaining-Time (SRT)");
            System.out.println("4. Round Robin (RR)");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");
            int choice = sc.nextInt();
            
            if (choice == 5) break;
            
            System.out.print("Enter number of processes: ");
            int n = sc.nextInt();
            List<Process> processes = new ArrayList<>();
            
            for (int i = 0; i < n; i++) {
                System.out.print("Enter Process ID: ");
                String id = sc.next();
                System.out.print("Enter Arrival Time: ");
                int arrivalTime = sc.nextInt();
                System.out.print("Enter Burst Time: ");
                int burstTime = sc.nextInt();
                processes.add(new Process(id, arrivalTime, burstTime));
            }
            
            if (choice == 1) fcfs(processes);
            else if (choice == 2) sjf(processes);
            else if (choice == 3) srt(processes);
            else if (choice == 4) {
                System.out.print("Enter Time Quantum: ");
                int quantum = sc.nextInt();
                roundRobin(processes, quantum);
            }
        }
    }
    
    static void fcfs(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int time = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        for (Process p : processes) {
            if (time < p.arrivalTime) time = p.arrivalTime;
            p.waitingTime = time - p.arrivalTime;
            ganttChart.append(p.id).append("(").append(time).append("-");
            time += p.burstTime;
            ganttChart.append(time).append(") ");
            p.turnaroundTime = p.waitingTime + p.burstTime;
        }
        System.out.println(ganttChart.toString());
        displayResults(processes);
    }
    
    static void sjf(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int time = 0;
        int completed = 0;
        int n = processes.size();
        List<Process> readyQueue = new ArrayList<>();
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        
        while (completed < n) {
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0 && !readyQueue.contains(p)) {
                    readyQueue.add(p);
                }
            }
            
            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }
            
            Process shortest = readyQueue.get(0);
            for (Process p : readyQueue) {
                if (p.burstTime < shortest.burstTime) {
                    shortest = p;
                }
            }
            
            ganttChart.append(shortest.id).append("(").append(time).append("-");
            time += shortest.burstTime;
            ganttChart.append(time).append(") ");
            
            shortest.completionTime = time;
            shortest.turnaroundTime = shortest.completionTime - shortest.arrivalTime;
            shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
            shortest.remainingTime = 0;
            
            readyQueue.remove(shortest);
            completed++;
        }
        
        System.out.println(ganttChart.toString());
        displayResults(processes);
    }
    
    static void srt(List<Process> processes) {
        int time = 0;
        int completed = 0;
        int n = processes.size();
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        Process current = null;
        
        List<Process> processList = new ArrayList<>(processes);
        processList.sort(Comparator.comparingInt(p -> p.arrivalTime));
        Queue<Process> readyQueue = new LinkedList<>();
        
        while (completed < n) {
            while (!processList.isEmpty() && processList.get(0).arrivalTime <= time) {
                readyQueue.add(processList.remove(0));
            }
            
            if (!readyQueue.isEmpty() && 
                (current == null || readyQueue.peek().remainingTime < current.remainingTime)) {
                if (current != null) {
                    readyQueue.add(current);
                }
                current = readyQueue.poll();
            }
            
            if (current == null) {
                ganttChart.append("Idle(").append(time).append("-");
                time++;
                ganttChart.append(time).append(") ");
                continue;
            }
            
            ganttChart.append(current.id).append("(").append(time).append("-");
            current.remainingTime--;
            time++;
            ganttChart.append(time).append(") ");
            
            if (current.remainingTime == 0) {
                current.completionTime = time;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                completed++;
                current = null;
            }
        }
        
        System.out.println(ganttChart.toString());
        displayResults(processes);
    }
    
    static void roundRobin(List<Process> processes, int quantum) {
        Queue<Process> queue = new LinkedList<>(processes);
        int time = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        while (!queue.isEmpty()) {
            Process p = queue.poll();
            ganttChart.append(p.id).append("(").append(time).append("-");
            if (p.remainingTime > quantum) {
                time += quantum;
                p.remainingTime -= quantum;
                queue.add(p);
            } else {
                time += p.remainingTime;
                p.remainingTime = 0;
                p.turnaroundTime = time - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
            }
            ganttChart.append(time).append(") ");
        }
        System.out.println(ganttChart.toString());
        displayResults(processes);
    }
    
    static void displayResults(List<Process> processes) {
        System.out.println("\nProcess\tArrival\tBurst\tWaiting\tTurnaround");
        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            System.out.println(p.id + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" + 
                             p.waitingTime + "\t" + p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.println("Average Waiting Time: " + (totalWT / processes.size()));
        System.out.println("Average Turnaround Time: " + (totalTAT / processes.size()));
    }
}