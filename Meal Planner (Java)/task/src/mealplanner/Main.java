package mealplanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
  public static void main(String[] args) throws SQLException {

    final String url = "jdbc:postgresql://localhost:5432/meals_db";
    final String user = "postgres";
    final String pass = "1111";

    Connection con = DriverManager.getConnection(url, user, pass);

    Statement statement = con.createStatement();

    //statement.executeUpdate("drop table if exists meals");
    //statement.executeUpdate("drop table if exists ingredients");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS meals (" +
            "category VARCHAR(255), " +
            "meal VARCHAR(255), " +
            "meal_id INT);");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients(" +
            "ingredient VARCHAR(255), " +
            "ingredient_id INT, " +
            "meal_id INT);");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS plan(" +
            "weekDay VARCHAR(255), " +
            "meal VARCHAR(255), " +
            "category VARCHAR(255), " +
            "meal_id INT);");




    Scanner scanner = new Scanner(System.in);
    String action;
    List<Meal> meals = new ArrayList<>();

    String showStatement = "SELECT category, meal, ingredient " +
            "FROM meals, ingredients " +
            "WHERE meals.meal_id = ingredients.meal_id";
    // Statement statement = con.createStatement();
    ResultSet rs = statement.executeQuery(showStatement);
    while(rs.next()){
      meals.add(new Meal(
              rs.getString("category"),
              rs.getString("meal"),
              rs.getString("ingredient")
      ));
    }
    statement.close();


    Meal meal;

    String updateMeals = "INSERT INTO meals (category, meal, meal_id) " +
            "VALUES (?, ?, ?)";
    String updateIngredients = "INSERT INTO ingredients (ingredient, ingredient_id, meal_id) " +
            "VALUES (?, ?, ?)";
    PreparedStatement insertMeal = con.prepareStatement(updateMeals);
    PreparedStatement insertIngredients = con.prepareStatement(updateIngredients);

    do {
      System.out.println("What would you like to do (add, show, plan, save, exit)?");
      action = scanner.nextLine();
      if(action.equals("add")) {
        meal = addMeal(scanner);
        System.out.println("The meal has been added!");
        meals.add(meal);


        insertMeal.setString(1, meal.getCategory());
        insertMeal.setString(2, meal.getName());
        insertMeal.setInt(3, meals.size() + 1);
        insertMeal.executeUpdate();

        insertIngredients.setString(1, meal.getIngredients());
        insertIngredients.setInt(2, meals.size() + 1);
        insertIngredients.setInt(3, meals.size() + 1);
        insertIngredients.executeUpdate();


      }
      else if(action.equals("show")) {
        show(scanner, con);
      }

      //week planning logic
      else if(action.equals("plan")) {
        planWeek(scanner, con);
      }

      else if(action.equals("save")) {
        savePlan(scanner, con);
      }

    }
    while(!(action.equals("exit")));

    if(action.equals("exit")) {
      insertMeal.close();
      insertIngredients.close();
      con.close();
      System.out.println("Bye!");
    }
  }

  public static Meal addMeal(Scanner input) {
    String category;
    String name;
    String ingredients;
    boolean catStatus = true;
    boolean nameStatus = true;
    boolean ingStatus = true;
    do {
      System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
      category = input.nextLine();
      if (!(category.equals("lunch") || category.equals("dinner") || category.equals("breakfast"))) {
        System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
      }
      else {
        catStatus = false;
      }
    }
    while(catStatus);

    do {
      System.out.println("Input the meal's name:");
      name = input.nextLine();
      if (!(name.matches("^[a-zA-Z]+( [a-zA-Z]+)*$"))) {
        System.out.println("Wrong format. Use letters only!");
      }
      else {
        nameStatus = false;
      }
    }
    while(nameStatus);

    do {
      System.out.println("Input the ingredients:");
      //Pattern p = Pattern.compile("^[a-zA-Z]+( [a-zA-Z]+)*$");
      ingredients = input.nextLine();

      if (!(ingredients.matches("^[A-Za-z ]+(?:[,][ A-Za-z ]+)*$"))) {
        System.out.println("Wrong format. Use letters only!");
      }
      else {
        ingStatus = false;
      }
    }
    while(ingStatus);

    return new Meal(category, name, ingredients);
  }

  public static void show(Scanner input, Connection con) throws SQLException {
    String category;
    boolean catStatus = true;

    do {
      System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
      category = input.nextLine();
      if (!(category.equals("lunch") || category.equals("dinner") || category.equals("breakfast"))) {
        System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
      }
      else {
        catStatus = false;
      }
    }
    while(catStatus);

    String showStatement = "SELECT meal, ingredient " +
            "FROM meals, ingredients " +
            "WHERE meals.meal_id = ingredients.meal_id AND meals.category='" +
            category + "'" +
            " ORDER BY meals.meal_id asc";
    Statement showState = con.createStatement();
    //showState.setString(1, category);
    ResultSet rs = showState.executeQuery(showStatement);

    if (!rs.isBeforeFirst() ) {
      System.out.println("No meals found.");
      return;
    }

    System.out.println("Category: " + category);
    while(rs.next()) {
      System.out.println("Name: " + rs.getString("meal") + "\n" + "Ingredients:" + "\n" + rs.getString("ingredient").replaceAll(",", "\n"));
    }
    showState.close();
  }

  public static void planWeek(Scanner scanner, Connection con) throws SQLException {



    String[] category = new String[]{"breakfast", "lunch", "dinner"};
    String[] weekDays = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    List<String> mealList = new ArrayList<>();
    String mealSelection;
    int meal_id = 0;
    Boolean mealExits = false;
    HashMap<String, Integer> mealMap = new HashMap<>();
    String mealOptions = "";
    String dropTable = "DELETE FROM plan";

    String planInsert = "INSERT INTO plan (meal, category, meal_id, weekDay) " +
            "VALUES (?, ?, ?, ?)";
    PreparedStatement insertPlan = con.prepareStatement(planInsert);

    String showPlan = "SELECT meal, category, meal_id, weekDay " +
            "FROM plan " +
            "WHERE weekDay = ";

    Statement showState = con.createStatement();
    showState.execute(dropTable);


    for(String wday: weekDays) {
      System.out.println(wday);
      for(String cat: category) {
        String showMealList = "SELECT meal, meals.meal_id " +
                "FROM meals, ingredients " +
                "WHERE meals.meal_id = ingredients.meal_id AND meals.category='" +
                cat + "'" +
                " ORDER BY meal asc";
        //showState.setString(1, category);
        ResultSet rs = showState.executeQuery(showMealList);


        while(rs.next()) {
          mealOptions = rs.getString("meal");
          meal_id = rs.getInt("meal_id");
          mealMap.put(mealOptions, meal_id);
          System.out.println(mealOptions);
          mealList.add(mealOptions);

        }

        System.out.println("Choose the " + cat + " for " + wday + " from the list above:");

        do {
          mealSelection = scanner.nextLine();
          if(mealList.contains(mealSelection)) {
            insertPlan.setString(1, mealSelection);
            insertPlan.setString(2, cat);
            insertPlan.setInt(3, mealMap.get(mealSelection));
            insertPlan.setString(4, wday);
            insertPlan.executeUpdate();
            //mealMap.put(cat, mealSelection);
            mealExits = false;
          }
          else {
            mealExits = true;
            System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
          }
        } while(mealExits);

      }
      System.out.println("Yeah! We planned the meals for " + wday + "." + "\n");

    }

    for( String wd: weekDays) {
      System.out.println(wd);
      ResultSet rs2 = showState.executeQuery(showPlan + "'" + wd + "'");
      while(rs2.next()) {
        String meal2 = rs2.getString("meal");
        String i = rs2.getString("category");
        System.out.println(i.substring(0,1).toUpperCase() + i.substring(1) + ": " + meal2);
      }
      System.out.println();
    }
    showState.close();
    insertPlan.close();
  }

  public static void savePlan(Scanner scanner, Connection con) throws SQLException{
    String filename;
    HashMap<String, Integer> ingVals = new HashMap<>();
    String saveStatement = "SELECT * FROM plan";
    String getIngs = "SELECT i.ingredient FROM plan p INNER JOIN meals o USING (meal_id) \n" +
            "INNER JOIN ingredients i using (meal_id)";
    Statement planTest = con.createStatement();
    ResultSet rs = planTest.executeQuery(saveStatement);
    if(!rs.isBeforeFirst()) {
      System.out.println("Unable to save. Plan your meals first.");
      return;
    }

    ResultSet rs1 = planTest.executeQuery(getIngs);
    while(rs1.next()){
      String ingList = rs1.getString("ingredient");
      String[] ingredients = ingList.split(",");
      for(String item: ingredients){
        item = item.trim();
        ingVals.put(item, ingVals.getOrDefault(item,0) + 1);
      }
    }


    System.out.println("Input a filename:");
    filename = scanner.nextLine();


    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
      for (String entry : ingVals.keySet()) {
        if(ingVals.get(entry) == 1) {
          writer.write(entry);
          writer.newLine();
        }
        else{
          writer.write(entry + " x" + ingVals.get(entry));
          writer.newLine();
        }

      }
      if((new File(filename)).exists()) {
        System.out.println("Saved!");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    finally {
      // Close all resources
      try {
        if (rs != null) rs.close();
        if (rs1 != null) rs1.close();
        if (planTest != null) planTest.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}