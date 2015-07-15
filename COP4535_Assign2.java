import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.lang.ArrayIndexOutOfBoundsException;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author stevefoo
 */
public class MazeTraversal {

    static LinkedList<Integer> WALL_VALUE = new LinkedList<>();//holds wall penalties

    public static void main(String[] args) {

        for (String str : args) {
            if (args.length <= 4) {
                System.out.println("ERROR invalid inputs");
                break;
            }
            try {
                if(str.equals("-p")||str.equals("-f"))
                    continue;
                if (!str.contains(".txt")) {
                    WALL_VALUE.add(Integer.parseInt(str));
                }
                else if(str.contains(".txt")) {
                    for (int value : WALL_VALUE) {
                        System.out.print(str + ": Penalty = "+value+"\nShortest path with penalty "+value+" costs ");
                        construct(str, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing" + str);
            } catch (NumberFormatException n) {//checks for wrong input being passed
                System.err.println("Error processing" + str);
            }
            System.out.println();
        }
    }

    public static void construct(String file, int wall_value) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(file));
        int rows = in.nextInt();
        int columns = in.nextInt();
        room[][] maze = new room[rows + 2][columns + 2];
        int row;
        int col;
        in.nextLine();
        for (int r = 1; r <= rows; r++) {//helps initialize all rooms created in the maze
            for (int c = 1; c <= columns; c++) {
                if (maze[r][c] == null) {
                    maze[r][c] = new room(r, c);
                }

            }
        }
        while (in.hasNext()) {
            String wall = "";
            String[] line = in.nextLine().split(" ");
            if(line.length<2){//ERROR CATCHING if input is less than 3 in length than it's not valid
                continue;            
            }
            row = Integer.parseInt(line[0]);
            col = Integer.parseInt(line[1]);
            if (line.length == 3) {// ERROR CATCHING if input array isn't length 3 then it hasn't been given a wall
                wall = line[2];
            }
            if (((row < 0 || row >= rows || col < 0 || col >= columns))) {//ERROR CATCHING if input dimensions exceed maze dimensions then skip 
                continue;
            }
            maze[row + 1][col + 1].wall = maze[row+1][col+1].wall.concat(wall);//concat the wall value with already created room
            wall(maze, maze[row + 1][col + 1], rows, columns);//for the room with the wall. establish that adjcacent rooms achknowledge the wall being there
        }
        closestToExit(maze, maze[1][1], rows, columns, wall_value);//Dijkstra's alogorithm
    }

    static void wall(room[][] maze, room a, int rows, int columns) {
        int row = a.row;
        int col = a.col;

        if (a.wall.contains("N") && (row - 1) != 0) {//if room contains a N then North room must have a S
                maze[row - 1][col].wall = maze[row - 1][col].wall.concat("S");
            
        }
        if (a.wall.contains("S") && row + 1 != rows + 1) {//if room contains a S then south room must have a N
                maze[row + 1][col].wall = maze[row + 1][col].wall.concat("N");
        }
        if (a.wall.contains("W") && col - 1 != 0) {//if room contains a W then west room must have a E
                maze[row][col - 1].wall = maze[row][col - 1].wall.concat("E");
        }
        if (a.wall.contains("E") && col + 1 != columns + 1) {//if room contains a E then east room must have a W
                maze[row][col + 1].wall = maze[row][col + 1].wall.concat("W");
        }

    }

    static void closestToExit(room[][] maze, room s, int end_rows, int end_col, int wall_value) {
        PriorityQueue<room> q = new PriorityQueue<>();
        int knock = 0;
        s.distance = 1;
        s.visited = true;
        adjacent(maze, s, wall_value);
        q.add(s);
        boolean end = false;
        while (!q.isEmpty() && !end) {
            room temp = q.poll();
            if (temp.row == end_rows && temp.col == end_col) {//if room row and col match endrow and endcol then it's reached the exit
                adjacent(maze,temp,wall_value);
                System.out.print(temp.distance+" squares\n"+"Number of wall knocked down: ");
                end = true;
                backtrack(temp);//print out path and # of walls knocked down
                break;
            }
            for (room curr : temp.adj) {//goes through adjaceny of temp room
                if (!curr.visited) {//has not been touched
                    if (curr.distance!=Double.POSITIVE_INFINITY) {//room was created by going through a wall
                        curr.visited = true;
                        adjacent(maze, curr, wall_value);//creates adjacent rooms for curr
                        curr.prev = temp;//establish previous room to help backtracking method
                        q.add(curr);
                    } else {//went through the regular way
                        curr.distance = temp.distance + 1;
                        curr.visited = true;
                        adjacent(maze, curr, wall_value);//creates adjacent rooms for curr
                        curr.prev=temp;//establish previous room to help backtracking method
                        q.add(curr);
                    }
                            
                }

            }
        }

    }

    static void adjacent(room[][] maze, room a, int wallval) {//creates adjacent rooms

        if (maze[a.row][a.col + 1] != null && !a.wall.contains("E")) {//if room is adjcant with no wall inbetween them
            a.adj.add(maze[a.row][a.col + 1]);
        } else if (maze[a.row][a.col + 1] != null && a.wall.contains("E")&&!maze[a.row][a.col + 1].visited) {//if room is adjacent with a wall inbetween then create a new room and add to queue
            room b = new room(maze[a.row][a.col + 1]);
            b.distance = a.distance + wallval + 1;
            a.adj.add(b);
        }
        if (maze[a.row][a.col - 1] != null && !a.wall.contains("W")) {//if room is adjcant with no wall inbetween them
            a.adj.add(maze[a.row][a.col - 1]);
        } else if (maze[a.row][a.col - 1] != null && a.wall.contains("W")&&!maze[a.row][a.col - 1].visited) {//if room is adjacent with a wall inbetween then create a new room and add to queue
            room b = new room(maze[a.row][a.col - 1]);
            b.distance = a.distance + wallval + 1;
            a.adj.add(b);
        }
        if (maze[a.row + 1][a.col] != null && !a.wall.contains("S")) {//if room is adjcant with no wall inbetween them
            a.adj.add(maze[a.row + 1][a.col]);
        } else if (maze[a.row + 1][a.col] != null && a.wall.contains("S")&&!maze[a.row + 1][a.col].visited) {//if room is adjacent with a wall inbetween then create a new room and add to queue
            room b = new room(maze[a.row + 1][a.col]);
            b.distance = a.distance + wallval + 1;
            a.adj.add(b);
        }
        if (maze[a.row - 1][a.col] != null && !a.wall.contains("N")) {//if room is adjcant with no wall inbetween them
            a.adj.add(maze[a.row - 1][a.col]);
        } else if (maze[a.row - 1][a.col] != null && a.wall.contains("N")&&!maze[a.row - 1][a.col].visited) {//if room is adjacent with a wall inbetween then create a new room and add to queue
            room b = new room(maze[a.row - 1][a.col]);
            b.distance = a.distance + wallval + 1;
            a.adj.add(b);
        }

    }
    static void backtrack(room a){//finds path from exit to start.
        int wall = 0;
        LinkedList<String> path = new LinkedList<>();
        while(a.distance!=1){// if value is 1 then we reached the start
            if (a.prev.row + 1 == a.row) {
                path.add("S");
                if(a.prev.wall.contains("S")){//if room came from south check to see if wall is inbetween
                    wall++;
                }
            } else if (a.prev.row - 1 == a.row) {
                path.add("N");
                if(a.prev.wall.contains("N")){//if room came from north check to see if wall is inbetween
                    wall++;
                }
            } else if (a.prev.col + 1 == a.col) {
                path.add("E");
                if(a.prev.wall.contains("E")){//if room came from east check to see if wall is inbetween
                    wall++;
                }
            } else if (a.prev.col - 1 == a.col) {
                path.add("W");
                if(a.prev.wall.contains("W")){//if room came from west check to see if wall is inbetween
                    wall++;
                }
            }
            a=a.prev;       
        }
        System.out.print(wall + "\n");
        for(int i =path.size()-1; i>=0;i--){//prints list backwards from start to exit
            System.out.print(path.get(i));
        }
        System.out.println();        
    }

    static class room implements Comparable<room> {
        int row;
        int col;
        double wall_value=Double.POSITIVE_INFINITY;
        String wall = "";
        PriorityQueue<room> adj = new PriorityQueue<room>();
        double distance = Double.POSITIVE_INFINITY;
        boolean visited;
        room prev;

        room(int row, int col) {
            this.row = row;
            this.col = col;
        }

        room(int row, int col, String wall) {
            this.row = row;
            this.col = col;
            this.wall = wall;
        }

        room(room other) {
            this.row = other.row;
            this.col = other.col;
            this.wall = other.wall;
            this.visited = other.visited;
            this.distance = other.distance;
            this.adj.clear();
            this.adj = new PriorityQueue<>(other.adj);
        }

        @Override
        public int compareTo(room o) {
            return (int) (this.distance - o.distance);
        }

    }

}
