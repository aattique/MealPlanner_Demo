package mealplanner;

public class Meal {

    private String category;
    private String name;
    private String ingredients;

    public Meal(String cat, String name, String ing) {
        this.category = cat;
        this.name = name;
        this.ingredients = ing;
    }

    public void setCategory(String cat) {
        this.category = cat;
    }
    public void setName(String na) {
        this.name = na;
    }
    public void setIngredients(String ing) {
        this.ingredients = ing;
    }
    public String getCategory() {
        return this.category;
    }
    public String getName() {
        return this.name;
    }
    public String getIngredients() {
        return this.ingredients;
    }

    public void printMeal() {
         System.out.println("Category: " + this.category + "\n" + "Name: " + this.name + "\n" + "Ingredients:" + "\n" + this.ingredients.replaceAll(",", "\n"));
    }

}
