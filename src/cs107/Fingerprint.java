package cs107;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides tools to compare fingerprint.
 */
public class Fingerprint {

    /**
     * The number of pixels to consider in each direction when doing the linear
     * regression to compute the orientation.
     */
    public static final int ORIENTATION_DISTANCE = 16;

    /**
     * The maximum distance between two minutiae to be considered matching.
     */
    public static final int DISTANCE_THRESHOLD = 5;

    /**
     * The number of matching minutiae needed for two fingerprints to be considered
     * identical.
     */
    public static final int FOUND_THRESHOLD = 20;

    /**
     * The distance between two angle to be considered identical.
     */
    public static final int ORIENTATION_THRESHOLD = 20;

    /**
     * The offset in each direction for the rotation to test when doing the
     * matching.
     */
    public static final int MATCH_ANGLE_OFFSET = 2;

    /**
     * Returns an array containing the value of the 8 neighbours of the pixel at
     * coordinates <code>(row, col)</code>.
     * <p>
     * The pixels are returned such that their indices corresponds to the following
     * diagram:<br>
     * ------------- <br>
     * | 7 | 0 | 1 | <br>
     * ------------- <br>
     * | 6 | _ | 2 | <br>
     * ------------- <br>
     * | 5 | 4 | 3 | <br>
     * ------------- <br>
     * <p>
     * If a neighbours is out of bounds of the image, it is considered white.
     * <p>
     * If the <code>row</code> or the <code>col</code> is out of bounds of the
     * image, the returned value should be <code>null</code>.
     *
     * @param image array containing each pixel's boolean value.
     * @param row   the row of the pixel of interest, must be between
     *              <code>0</code>(included) and
     *              <code>image.length</code>(excluded).
     * @param col   the column of the pixel of interest, must be between
     *              <code>0</code>(included) and
     *              <code>image[row].length</code>(excluded).
     * @return An array containing each neighbours' value.
     */
    public static boolean[] getNeighbours(boolean[][] image, int row, int col) {
        assert (image != null); // special case that is not expected (the image is supposed to have been checked
        if (row > (image.length - 1) || row < 0 || col > (image[0].length - 1) || col < 0) {
            return null;
        }
        return new boolean[]{getPixel(image, row - 1, col),
                getPixel(image, row - 1, col + 1),
                getPixel(image, row, col + 1),
                getPixel(image, row + 1, col + 1),
                getPixel(image, row + 1, col),
                getPixel(image, row + 1, col - 1),
                getPixel(image, row, col - 1),
                getPixel(image, row - 1, col - 1)};
    }

    /**
     * used by getNeighbours
     *
     * @param image array containing each pixel's boolean value.
     * @param row   the row of the pixel
     * @param col   the rcol of the pixel
     * @return the pixel value either true or false (black or white) or false if pixel's isn't in the image
     */
    static boolean getPixel(boolean[][] image, int row, int col) {
        try {
            return image[row][col];
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

    }

    /**
     * Computes the number of black (<code>true</code>) pixels among the neighbours
     * of a pixel.
     *
     * @param neighbours array containing each pixel value. The array must respect
     *                   the convention described in
     *                   {@link #getNeighbours(boolean[][], int, int)}.
     * @return the number of black neighbours.
     */
    public static int blackNeighbours(boolean[] neighbours) {
        int blackNeighCount = 0;
        for (boolean neighbour : neighbours) {
            if (neighbour) {
                blackNeighCount++;
            }
        }
        return blackNeighCount;
    }

    /**
     * Computes the number of white to black transitions among the neighbours of
     * pixel.
     *
     * @param neighbours array containing each pixel value. The array must respect
     *                   the convention described in
     *                   {@link #getNeighbours(boolean[][], int, int)}.
     * @return the number of white to black transitions.
     */
    public static int transitions(boolean[] neighbours) {
        int transitionCount = 0;
        for (int i = 0; i < neighbours.length - 1; i++) {
            if ((!neighbours[i]) && neighbours[i + 1]) {
                transitionCount++;
            }
        }
        //check special case for transition p7 to p0
        if ((!neighbours[neighbours.length - 1]) && neighbours[0]) {
            transitionCount++;
        }

        return transitionCount;
    }

    /**
     * Returns <code>true</code> if the images are identical and false otherwise.
     *
     * @param image1 array containing each pixel's boolean value.
     * @param image2 array containing each pixel's boolean value.
     * @return <code>True</code> if they are identical, <code>false</code>
     * otherwise.
     */
    public static boolean identical(boolean[][] image1, boolean[][] image2) {
        for (int row = 0; row < image1.length; row++) {
            for (int col = 0; col < image1[0].length; col++) {
                if (image1[row][col] != image2[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * returns a copy of the image (bidimensional table)
     *
     * @param image nested array containg each pixel value
     * @return the copy of the given image
     */
    static boolean[][] copyImage(boolean[][] image) {
        boolean[][] newImage = new boolean[image.length][image[0].length];
        for (int row = 0; row < image.length; row++) {
            for (int col = 0; col < image[0].length; col++) {
                newImage[row][col] = image[row][col];
            }
        }
        return newImage;
    }


    /**
     * Test a few criteria to determine if a pixel is necessary or not
     *
     * @param image nested array containg each pixel value
     * @param row   row of the pixel of interest
     * @param col   col of the pixel of interest
     * @param step  which criteria to test (either 0 or 1 )
     * @return return true if pixel is not necessary, false if it's necessary
     */
    static boolean checksSteps(boolean[][] image, int row, int col, int step) {
        boolean pixel = image[row][col];
        boolean[] neighPixel = getNeighbours(image, row, col);
        int bNeighPixel = blackNeighbours(neighPixel);

        //checks
        boolean pNeighNonNull = neighPixel != null;
        boolean bNeighInterval = ((bNeighPixel <= 6) && (bNeighPixel >= 2));
        boolean transitionsNumber = (transitions(neighPixel) == 1);
        boolean white024 = (!(neighPixel[0] && neighPixel[2] && neighPixel[4]));
        boolean white246 = (!(neighPixel[2] && neighPixel[4] && neighPixel[6]));

        boolean white026 = (!(neighPixel[0] && neighPixel[2] && neighPixel[6]));
        boolean white046 = (!(neighPixel[0] && neighPixel[4] && neighPixel[6]));

        if (pixel && pNeighNonNull && bNeighInterval && transitionsNumber && white024 && white246 && step == 0) {
            return true;
        } else
            return pixel && pNeighNonNull && bNeighInterval && transitionsNumber && white046 && white026 && step == 1;
    }

    /**
     * Internal method used by {@link #thin(boolean[][])}.
     *
     * @param image array containing each pixel's boolean value.
     * @param step  the step to apply, Step 0 or Step 1.
     * @return A new array containing each pixel's value after the step.
     */


    public static boolean[][] thinningStep(boolean[][] image, int step) {
        boolean[][] imageToModify = copyImage(image);
        for (int row = 0; row < image.length; row++) {
            for (int col = 0; col < image[0].length; col++) {
                if (checksSteps(image, row, col, step)) {
                    imageToModify[row][col] = false;
                }
            }
        }
        return imageToModify;
    }

    /**
     * Compute the skeleton of a boolean image.
     *
     * @param image array containing each pixel's boolean value.
     * @return array containing the boolean value of each pixel of the image after
     * applying the thinning algorithm.
     */

    public static boolean[][] thin(boolean[][] image) {
        boolean[][] newImage = copyImage(image);
        boolean[][] oldImage;
        do {
            oldImage = newImage;
            newImage = thinningStep(thinningStep(oldImage, 0), 1);
        } while (!(identical(newImage, oldImage)));
        return newImage;
    }

    /**
     * return true if pixel is in a given square portion of the image
     *
     * @param row      row of minutia
     * @param col      col of minutia
     * @param distance distance from center of minutia
     * @param y        row of pixel of interest
     * @param x        col of pixel of interest
     * @return true if pixel is inside the "square around the minutia of given distance else false
     */
    public static boolean isInsideSquare(boolean[][] image, int row, int col, int distance, int y, int x) {
        int xLowDist = Math.max(col - distance, 0);
        int xHighDist = Math.min(col + distance, (image[0].length - 1));
        int yLowDist = Math.max(row - distance, 0);
        int yHighDist = Math.min(row + distance, (image.length - 1));
        if (y < yLowDist || y > yHighDist) {
            return false;
        } else return x >= xLowDist && x <= xHighDist;
    }

    static boolean[][] createOnlyMinutieImage(boolean[][] image, int row, int col) {
        boolean[][] returnImage = copyImage(image);
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[0].length; x++) {
                returnImage[y][x] = false;
            }
        }
        returnImage[row][col] = true;
        return returnImage;
    }

    /**
     * Computes all pixels that are connected to the pixel at coordinate
     * <code>(row, col)</code> and within the given distance of the pixel.
     *
     * @param image    array containing each pixel's boolean value.
     * @param row      the first coordinate of the pixel of interest.
     * @param col      the second coordinate of the pixel of interest.
     * @param distance the maximum distance at which a pixel is considered.
     * @return An array where <code>true</code> means that the pixel is within
     * <code>distance</code> and connected to the pixel at
     * <code>(row, col)</code>.
     */


    public static boolean[][] connectedPixels(boolean[][] image, int row, int col, int distance) {
        boolean[][] minutie = createOnlyMinutieImage(image, row, col);
        boolean[][] oldMinutie;
        do {
            oldMinutie = copyImage(minutie);
            for (int y = 0; y < image.length; y++) {
                for (int x = 0; x < image[0].length; x++) {
                    if (isInsideSquare(image, row, col, distance, y, x) && image[y][x]) {
                        if (blackNeighbours(getNeighbours(minutie, y, x)) >= 1) {
                            minutie[y][x] = true;
                        }
                    }
                }
            }
            //runs as long as there are changes made to minutie
        } while (!identical(oldMinutie, minutie));
        return minutie;
    }

    /**
     * calculate the column of a pixel taking that the minutie is at column 0
     *
     * @param col  column of the pixel
     * @param colM column of the minutia
     * @return the colum (x) of the pixel with minutia as the origin
     */
    static int calculateX(int col, int colM) {
        return col - colM;
    }

    /**
     * calculate the row  of a pixel taking that the minutie is at column 0
     *
     * @param row  row of the pixel
     * @param rowM column of the minutia
     * @return the row (y) of the pixel with minutia as the origin
     */
    static int calculateY(int row, int rowM) {
        return rowM - row;
    }

    /**
     * Computes the slope of a minutia using linear regression.
     *
     * @param connectedPixels the result of
     *                        {@link #connectedPixels(boolean[][], int, int, int)}.
     * @param row             the row of the minutia.
     * @param col             the col of the minutia.
     * @return the slope.
     */

    public static double computeSlope(boolean[][] connectedPixels, int row, int col) {
        double sumXY = 0;
        double sumSqrX = 0;
        double sumSqrY = 0;

        for (int y = 0; y < connectedPixels.length; y++) {
            for (int x = 0; x < connectedPixels[0].length; x++) {
                if (connectedPixels[y][x]) {
                    sumXY += (calculateX(x, col) * calculateY(y, row));
                    sumSqrX += Math.pow(calculateX(x, col), 2);
                    sumSqrY += Math.pow(calculateY(y, row), 2);
                }
            }
        }
        if (sumSqrX == 0) {
            return Double.POSITIVE_INFINITY;
        } else if (sumSqrX >= sumSqrY) {
            return sumXY / sumSqrX;
        } else {
            return sumSqrY / sumXY;
        }
    }

    /**
     * //controle si un pixel de coordonnée (y,x) est au dessu de la droite de pente
     *
     * @param y     row of the pixe3l
     * @param x     column of the pixel
     * @param slope pente de la droite
     * @return true if the pixel(y,x) is in the upside of the droite de pente slope
     */
    static boolean isInTheUpside(int y, int x, double slope) {
        double newSlope = -1 / slope;
        return y >= (newSlope * x);
    }

    /**
     * Computes the orientation of a minutia in radians.
     *
     * @param connectedPixels the result of
     *                        {@link #connectedPixels(boolean[][], int, int, int)}.
     * @param row             the row of the minutia.
     * @param col             the col of the minutia.
     * @param slope           the slope as returned by
     *                        {@link #//computeSlope(boolean[][], int, int, int)}.
     * @return the orientation of the minutia in radians.
     */

    public static double computeAngle(boolean[][] connectedPixels, int row, int col, double slope) {
        int upSideCount = 0;
        int downSideCount = 0;
        double angle = Math.atan(slope);

        for (int y = 0; y < connectedPixels.length; y++) {
            for (int x = 0; x < connectedPixels[0].length; x++) {
                if (connectedPixels[y][x]) {
                    if (isInTheUpside(calculateY(y, row), calculateX(x, col), slope)) {
                        upSideCount++;
                    } else {
                        downSideCount++;
                    }
                }
            }
        }

        if (angle == Double.POSITIVE_INFINITY) {
            if (upSideCount > downSideCount) {
                return Math.PI / 2;
            } else {
                return -(Math.PI / 2);
            }
        } else if ((angle > 0) && (downSideCount > upSideCount)) {
            return angle + Math.PI;
        } else if ((angle < 0) && (downSideCount < upSideCount)) {
            return angle + Math.PI;
        } else {
            return angle;
        }

    }

    /**
     * Computes the orientation of the minutia that the coordinate <code>(row,
     * col)</code>.
     *
     * @param image    array containing each pixel's boolean value.
     * @param row      the first coordinate of the pixel of interest.
     * @param col      the second coordinate of the pixel of interest.
     * @param distance the distance to be considered in each direction to compute
     *                 the orientation.
     * @return The orientation in degrees.
     */
    public static int computeOrientation(boolean[][] image, int row, int col, int distance) {
        boolean[][] connectedPixelResult = connectedPixels(image, row, col, distance);
        double slope = computeSlope(connectedPixelResult, row, col);
        double angle = computeAngle(connectedPixelResult, row, col, slope);
        double degreeAngle = Math.toDegrees(angle);
        if (degreeAngle < 0) {
            degreeAngle += 360;
        }
        return (int) Math.round(degreeAngle);

    }

    /**
     * Extracts the minutiae from a thinned image.
     *
     * @param image array containing each pixel's boolean value.
     * @return The list of all minutiae. A minutia is represented by an array where
     * the first element is the row, the second is column, and the third is
     * the angle in degrees.
     * @see #thin(boolean[][])
     */
    public static List<int[]> extract(boolean[][] image) {
        ArrayList<int[]> minuties = new ArrayList<>();

        int transitionsNumber;
        for (int y = 1; y < image.length - 1; y++) {
            for (int x = 1; x < image[0].length - 1; x++) {
                transitionsNumber = transitions(getNeighbours(image, y, x));
                if (image[y][x]) {
                    if (transitionsNumber == 1 || transitionsNumber == 3) {
                        int z = computeOrientation(image, y, x, ORIENTATION_DISTANCE);
                        minuties.add(new int[]{y, x, z});
                    }
                }
            }
        }
        return minuties;
    }

    /**
     * Applies the specified rotation to the minutia.
     *
     * @param minutia   the original minutia.
     * @param centerRow the row of the center of rotation.
     * @param centerCol the col of the center of rotation.
     * @param rotation  the rotation in degrees.
     * @return the minutia rotated around the given center.
     */
    public static int[] applyRotation(int[] minutia, int centerRow, int centerCol, int rotation) {
        double angleRad = Math.toRadians(rotation);
        int x = minutia[1] - centerCol;
        int y = (centerRow - minutia[0]);
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        double newX = (x * cos) - (y * sin);
        double newY = (x * sin) + (y * cos);
        int newRow = (int) Math.round((centerRow - newY));
        int newCol = (int) Math.round((newX + centerCol));
        int newOrientation = Math.round((minutia[2] + rotation) % 360);
        return new int[]{newRow, newCol, newOrientation};

    }

    /**
     * Applies the specified translation to the minutia.
     *
     * @param minutia        the original minutia.
     * @param rowTranslation the translation along the rows.
     * @param colTranslation the translation along the columns.
     * @return the translated minutia.
     */
    public static int[] applyTranslation(int[] minutia, int rowTranslation, int colTranslation) {
        int newRow = minutia[0] - rowTranslation;
        int newCol = minutia[1] - colTranslation;
        int newOrientation = minutia[2];
        return new int[]{newRow, newCol, newOrientation};
    }

    /**
     * Computes the row, column, and angle a3fter applying a transformation
     * (translation and rotation).
     *
     * @param minutia        the original minutia.
     * @param centerCol      the column around which the point is rotated.
     * @param centerRow      the row around which the point is rotated.
     * @param rowTranslation the vertical translation.
     * @param colTranslation the horizontal translation.
     * @param rotation       the rotation.
     * @return the transformed minutia.
     */

    public static int[] applyTransformation(int[] minutia, int centerRow, int centerCol, int rowTranslation, int colTranslation, int rotation) {
        int[] rotatedMinutia = applyRotation(minutia, centerRow, centerCol, rotation);
        return applyTranslation(rotatedMinutia, rowTranslation, colTranslation);
    }

    /**
     * Computes the row, column, and angle after applying a transformation
     * (translation and rotation) for each minutia in the given list.
     *
     * @param minutiae       the list of minutiae.
     * @param centerCol      the column around which the point is rotated.
     * @param centerRow      the row around which the point is rotated.
     * @param rowTranslation the vertical translation.
     * @param colTranslation the horizontal translation.
     * @param rotation       the rotation.
     * @return the list of transformed minutiae.
     */
    public static List<int[]> applyTransformation(List<int[]> minutiae, int centerRow, int centerCol, int rowTranslation, int colTranslation, int rotation) {
        ArrayList<int[]> transformedMinutiae = new ArrayList<>();
        for (int[] ints : minutiae) {
            transformedMinutiae.add(applyTransformation(ints, centerRow, centerCol, rowTranslation, colTranslation, rotation));
        }
        return transformedMinutiae;
    }

    /**
     * Counts the number of overlapping minutiae.
     *
     * @param minutiae1      the first set of minutiae.
     * @param minutiae2      the second set of minutiae.
     * @param maxDistance    the maximum distance between two minutiae to consider
     *                       them as overlapping.
     * @param maxOrientation the maximum difference of orientation between two
     *                       minutiae to consider them as overlapping.
     * @return the number of overlapping minutiae.
     */
    public static int matchingMinutiaeCount(List<int[]> minutiae1, List<int[]> minutiae2, int maxDistance, int maxOrientation) {
        int matchingCount = 0;
        for (int[] m1 : minutiae1) {
            for (int[] m2 : minutiae2) {
                double rowDiff = m1[0] - m2[0];
                double colDiff = m1[1] - m2[1];
                int orientationDiff = Math.abs(m1[2] - m2[2]);
                double euclideanDist = Math.sqrt((Math.pow(rowDiff, 2) + Math.pow(colDiff, 2)));
                boolean distCheck = euclideanDist <= maxDistance;
                boolean orientationCheck = orientationDiff <= maxOrientation;
                if (distCheck && orientationCheck) {
                    matchingCount++;
                }
            }
        }
        return matchingCount;
    }

    /**
     * Compares the minutiae from two fingerprints.
     *
     * @param minutiae1 the list of minutiae of the first fingerprint.
     * @param minutiae2 the list of minutiae of the second fingerprint.
     * @return Returns <code>true</code> if they match and <code>false</code>
     * otherwise.
     */
    public static boolean match(List<int[]> minutiae1, List<int[]> minutiae2) {
        for (int m1 = 0; m1 < minutiae1.size(); m1++) {
            for (int m2 = 0; m2 < minutiae2.size(); m2++) {
                int rowTranslation = minutiae2.get(m2)[0] - minutiae1.get(m1)[0];
                int colTranslation = minutiae2.get(m2)[1] - minutiae1.get(m1)[1];
                int centerRow = minutiae1.get(m1)[0];
                int centerCol = minutiae1.get(m1)[1];
                int rotation = minutiae2.get(m2)[2] - minutiae1.get(m1)[2];
                for (int r = rotation - MATCH_ANGLE_OFFSET; r <= rotation + MATCH_ANGLE_OFFSET; r++) {
                    List<int[]> transformedMinutiae = applyTransformation(minutiae2, centerRow, centerCol, rowTranslation, colTranslation, r);
                    int matchCount = matchingMinutiaeCount(minutiae1, transformedMinutiae, DISTANCE_THRESHOLD, ORIENTATION_THRESHOLD);
                    if (matchCount >= FOUND_THRESHOLD) {

                        return true;
                    }
                }
            }

        }
        return false;
    }
}
