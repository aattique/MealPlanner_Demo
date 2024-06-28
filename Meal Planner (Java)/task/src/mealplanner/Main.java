package mealplanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {

    Scanner scanner = new Scanner(System.in);
    String action;
    List<Meal> meals = new ArrayList<>();
    Meal meal;
    do {
      System.out.println("What would you like to do (add, show, exit)?");
      action = scanner.nextLine();
      if(action.equals("add")) {
        meal = addMeal(scanner);
        System.out.println("The meal has been added!");
        meals.add(meal);
      }
      else if(action.equals("show")) {
        if (meals.isEmpty()) {
          System.out.println("No meals saved. Add a meal first.");
        }
        else show(meals);
      }
    }
    while(!(action.equals("exit")));
    if(action.equals("exit")) {
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

        if (!(ingredients.matches("^[A-Za-z]+(?:,\\s[A-Za-z ]+)*$"))) {
          System.out.println("Wrong format. Use letters only!");
        }
        else {
          ingStatus = false;
        }
    }
    while(ingStatus);

    return new Meal(category, name, ingredients);
  }

  public static void show(List<Meal> meals) {
    for(Meal meal: meals) {
      System.out.println("Category: " + meal.getCategory() + "\n" + "Name: " + meal.getName() + "\n" + "Ingredients:" + "\n" + meal.getIngredients().replaceAll(",", "\n"));
    }
  }

}