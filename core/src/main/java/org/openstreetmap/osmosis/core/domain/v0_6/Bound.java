// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;

/**
 * A data class representing an OSM data bound element.
 * 
 * @author Karl Newman
 */
public class Bound extends Entity implements Comparable<Bound> {
	
	private static final double MIN_LATITUDE = -90.0;
	private static final double MAX_LATITUDE = 90.0;
	private static final double MIN_LONGITUDE = -180.0;
	private static final double MAX_LONGITUDE = 180.0;
	
	private double right;
	private double left;
	private double top;
	private double bottom;
	private String origin;
	
	
	/**
	 * Creates a new instance which covers the entire planet.
	 * 
	 * @param origin
	 *            The origin (source) of the data, typically a URI
	 * 
	 */
	public Bound(String origin) {
		this(MAX_LONGITUDE, MIN_LONGITUDE, MAX_LATITUDE, MIN_LATITUDE, origin);
	}


	/**
	 * Creates a new instance with the specified boundaries.
	 * 
	 * @param right
	 *            The longitude coordinate of the right (East) edge of the bound
	 * @param left
	 *            The longitude coordinate of the left (West) edge of the bound
	 * @param top
	 *            The latitude coordinate of the top (North) edge of the bound
	 * @param bottom
	 *            The latitude coordinate of the bottom (South) edge of the bound
	 * @param origin
	 *            The origin (source) of the data, typically a URI
	 */
	public Bound(double right, double left, double top, double bottom, String origin) {
		super(new CommonEntityData(0, 0, new Date(), OsmUser.NONE, 0)); // minimal underlying entity
		
		// Check if any coordinates are out of bounds
		if (Double.compare(right, MAX_LONGITUDE + 1.0d) > 0
		        || Double.compare(right, MIN_LONGITUDE - 1.0d) < 0
		        || Double.compare(left, MAX_LONGITUDE + 1.0d) > 0
		        || Double.compare(left, MIN_LONGITUDE - 1.0d) < 0
		        || Double.compare(top, MAX_LATITUDE + 1.0d) > 0
		        || Double.compare(top, MIN_LATITUDE - 1.0d) < 0
		        || Double.compare(bottom, MAX_LATITUDE + 1.0d) > 0
		        || Double.compare(bottom, MIN_LATITUDE - 1.0d) < 0) {
			throw new IllegalArgumentException("Bound coordinates outside of valid range");
		}
		if (Double.compare(top, bottom) < 0) {
			throw new IllegalArgumentException("Bound top < bottom");
		}
		this.right = right;
		this.left = left;
		this.top = top;
		this.bottom = bottom;
		this.origin = origin;
	}


	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers within the store.
	 */
	public Bound(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);

		this.right = sr.readDouble();
		this.left = sr.readDouble();
		this.top = sr.readDouble();
		this.bottom = sr.readDouble();
		this.origin = sr.readString();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);

		sw.writeDouble(right);
		sw.writeDouble(left);
		sw.writeDouble(top);
		sw.writeDouble(bottom);
		sw.writeString(origin);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Bound;
	}


	/**
	 * @return The right (East) bound longitude
	 */
	public double getRight() {
		return right;
	}


	/**
	 * @return The left (West) bound longitude
	 */
	public double getLeft() {
		return left;
	}


	/**
	 * @return The top (North) bound latitude
	 */
	public double getTop() {
		return top;
	}


	/**
	 * @return The bottom (South) bound latitude
	 */
	public double getBottom() {
		return bottom;
	}


	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}


	/**
	 * Calculate the intersected area of this with the specified bound.
	 * 
	 * @param intersectingBound
	 *            Bound element with which to calculate the intersection
	 * @return Bound Resultant intersection of the two bound object
	 */
	public Bound intersect(Bound intersectingBound) {
		String newOrigin;
		double newRight = 0.0, newLeft = 0.0, newTop, newBottom;

		boolean intersect180, this180; // flags to indicate bound cross antimeridian

		if (intersectingBound == null) {
			return null; // no intersection
		}
		// first check the vertical intersection
		newTop = Math.min(this.getTop(), intersectingBound.getTop());
		newBottom = Math.max(this.getBottom(), intersectingBound.getBottom());
		if (Double.compare(newBottom, newTop) >= 0) { // no north-south intersecting region
			return null;
		}

		intersect180 = (Double.compare(intersectingBound.getLeft(), intersectingBound.getRight()) > 0);
		this180 = (Double.compare(this.getLeft(), this.getRight()) > 0);

		if ((intersect180 && this180) || !(intersect180 || this180)) {
			// if both or neither cross the antimeridian, use the simple case
			newRight = Math.min(this.getRight(), intersectingBound.getRight());
			newLeft = Math.max(this.getLeft(), intersectingBound.getLeft());
			if (!(intersect180 || this180) && (Double.compare(newLeft, newRight) >= 0)) {
				/*
				 * This is only applicable for the case where neither cross the antimeridian,
				 * because if both cross, they must intersect.
				 */
				return null; // no intersecting area
			}
		} else {
			Bound b1, b2; // stand-ins for this and intersectingBound

			if (intersect180 && !this180) {
				// passed parameter Bound crosses the antimeridian, this Bound doesn't
				b1 = this;
				b2 = intersectingBound;
			} else {
				// this Bound crosses the antimeridian, passed parameter Bound doesn't
				b1 = intersectingBound;
				b2 = this;
			}
			if (Double.compare(b1.getRight(), b2.getLeft()) > 0
			        && Double.compare(b1.getLeft(), b2.getRight()) < 0) {
				// intersects on both sides of the antimeridian--just pick the smaller of the
				// two
				Double diff1 = b1.getRight() - b1.getLeft();
				Double diff2 = b2.getRight() - MIN_LONGITUDE + MAX_LONGITUDE - b2.getLeft();
				if (Double.compare(diff1, diff2) <= 0) {
					newRight = b1.getRight();
					newLeft = b1.getLeft();
				} else {
					newRight = b2.getRight();
					newLeft = b2.getLeft();
				}
			} else if (Double.compare(b1.getRight(), b2.getLeft()) > 0) {
				// intersects on the East side of the antimeridian
				newRight = b1.getRight();
				newLeft = b2.getLeft();
			} else if (Double.compare(b1.getLeft(), b2.getRight()) < 0) {
				// intersects on the West side of the antimeridian
				newRight = b2.getRight();
				newLeft = b1.getLeft();
			}
		}
		if (Double.compare(newRight, newLeft) == 0) {
			return null;
		}
		
		// Keep the origin string from this if it's not blank, otherwise use the origin string from
		// the intersecting Bound
		if (origin != "") {
			newOrigin = origin;
		} else {
			newOrigin = intersectingBound.origin;
		}
		
		return new Bound(newRight, newLeft, newTop, newBottom, newOrigin);
	}


	/**
	 * Calculate the union area of this with the specified bound. Not a strict mathematical union,
	 * but the smallest rectangular area which includes both bound. Thus, result may include areas
	 * not contained in the original bound.
	 * 
	 * @param unionBound
	 *            Bound element with which to calculate the union
	 * @return Bound Resultant union of the two bound objects
	 */
	public Bound union(Bound unionBound) {
		double newRight = 0.0, newLeft = 0.0, newTop, newBottom;
		String newOrigin;

		if (unionBound == null) {
			return this; // nothing to compute a union with
		}

		// First compute the vertical union
		newTop = Math.max(this.getTop(), unionBound.getTop());
		newBottom = Math.min(this.getBottom(), unionBound.getBottom());
		if (Double.compare(newBottom, newTop) >= 0) { // no north-south intersecting region
			return null;
		}
		// Next check the (likely) common case where one of the bound covers the planet
		if ((Double.compare(this.getLeft(), MIN_LONGITUDE) == 0 && Double.compare(
		        this.getRight(),
		        MAX_LONGITUDE) == 0)
		        || (Double.compare(unionBound.getLeft(), MIN_LONGITUDE) == 0 && Double.compare(
		                unionBound.getRight(),
		                MAX_LONGITUDE) == 0)) {
			newRight = MAX_LONGITUDE;
			newLeft = MIN_LONGITUDE;
		} else {
			boolean union180, this180; // flags to indicate bound cross antimeridian
			double size1, size2; // resulting union sizes for comparison

			union180 = (Double.compare(unionBound.getLeft(), unionBound.getRight()) > 0);
			this180 = (Double.compare(this.getLeft(), this.getRight()) > 0);

			if (union180 && this180) {
				// if both cross the antimeridian, then the union will cross, too.
				newRight = Math.max(this.getRight(), unionBound.getRight());
				newLeft = Math.min(this.getLeft(), unionBound.getLeft());
			} else if (!(union180 || this180)) {
				// neither cross the antimeridian, but the union might

				// first calculate the size of a simple union which doesn't cross the antimeridian
				size1 = Math.max(this.getRight(), unionBound.getRight())
				        - Math.min(this.getLeft(), unionBound.getLeft());
				// then calculate the size of the resulting union which does cross the antimeridian
				size2 = (Math.min(this.getRight(), unionBound.getRight()) - MIN_LONGITUDE)
				        + (MAX_LONGITUDE - Math.max(this.getLeft(), unionBound.getLeft()));

				// now pick the smaller of the two
				if (Double.compare(size1, size2) <= 0) {
					newRight = Math.max(this.getRight(), unionBound.getRight());
					newLeft = Math.min(this.getLeft(), unionBound.getLeft());
				} else {
					newRight = Math.min(this.getRight(), unionBound.getRight());
					newLeft = Math.max(this.getLeft(), unionBound.getLeft());
				}
			} else {
				// One of the Bound crosses the antimeridian, the other doesn't
				Bound b1, b2;
				if (union180 && !this180) {
					// passed parameter Bound crosses the antimeridian, this Bound doesn't
					b1 = unionBound;
					b2 = this;
				} else {
					// this Bound crosses the antimeridian, passed parameter Bound doesn't
					b1 = this;
					b2 = unionBound;
				}

				// check for the case where the two Bound overlap on both edges such that the union
				// covers the planet.
				if (Double.compare(b1.getRight(), b2.getLeft()) >= 0
				        && Double.compare(b1.getLeft(), b2.getRight()) <= 0) {
					newLeft = MIN_LONGITUDE;
					newRight = MAX_LONGITUDE;
				} else {
					// first calculate the size of a union with the simple bound added to the left
					size1 = (Math.max(b1.getRight(), b2.getRight()) - MIN_LONGITUDE)
					        + (MAX_LONGITUDE - b1.getLeft());
					// first calculate the size of a union with the simple bound added to the right
					size2 = (b1.getRight() - MIN_LONGITUDE)
					        + (MAX_LONGITUDE - Math.min(b1.getLeft(), b2.getLeft()));

					// now pick the smaller of the two
					if (Double.compare(size1, size2) <= 0) {
						newRight = Math.max(b1.getRight(), b2.getRight());
						newLeft = b1.getLeft();
					} else {
						newRight = b1.getRight();
						newLeft = Math.min(b1.getLeft(), b2.getLeft());
					}
				}
			}
		}

		if (Double.compare(newRight, newLeft) == 0) {
			return null;
		}
		
		// Keep the origin string from this if it's not blank, otherwise use the origin string from
		// the union Bound
		if (this.getOrigin() != null && !this.getOrigin().equals("")) {
			newOrigin = getOrigin();
		} else {
			newOrigin = unionBound.getOrigin();
		}
		
		return new Bound(newRight, newLeft, newTop, newBottom, newOrigin);
	}


	/**
	 * Retrieve a collection of Bound objects which collectively comprise the entirety of this
	 * Bound but individually do not cross the antimeridian and thus can be used in simple area
	 * operations. The degenerate case will return this Bound.
	 * 
	 * @return Iterable collection of Bound elements
	 */
	public Iterable<Bound> toSimpleBound() {
		Collection<Bound> c = new LinkedList<Bound>();
		if (Double.compare(this.getLeft(), this.getRight()) < 0) {
			// simple case, just return this
			c.add(this);
		} else {
			// split the bound into two parts--one on either side of the antimeridian
			c.add(new Bound(
			        MAX_LONGITUDE,
			        this.getLeft(),
			        this.getTop(),
			        this.getBottom(),
			        this.getOrigin()));
			c.add(new Bound(
			        this.getRight(),
			        MIN_LONGITUDE,
			        this.getTop(),
			        this.getBottom(),
			        this.getOrigin()));
		}
		return Collections.unmodifiableCollection(c);
	}


	/**
	 * Compares this bound to the specified bound. The bound comparison is based on a comparison
	 * of area, latitude, and longitude in that order.
	 * 
	 * @param comparisonBound
	 *            The bound to compare to.
	 * @return 0 if equal, < 0 if this sorts before comparison (this is "smaller"), and > 0 if this
	 *         sorts before comparison (this is "bigger")
	 */
	public int compareTo(Bound comparisonBound) {
		double areaT = 0.0, areaC = 0.0;
		int result;

		/*
		 * This is a very simple "area" calculation just using the coordinate values, not accounting
		 * for any projections.
		 */
		for (Bound b : this.toSimpleBound()) {
			areaT += (b.getRight() - b.getLeft()) * (b.getTop() - b.getBottom());
		}
		for (Bound b : comparisonBound.toSimpleBound()) {
			areaC += (b.getRight() - b.getLeft()) * (b.getTop() - b.getBottom());
		}

		// Use Double.compare (instead of < and >) to catch unique border cases
		result = Double.compare(areaT, areaC);
		if (result != 0) {
			return result;
		}

		result = Double.compare(this.getTop(), comparisonBound.getTop());
		if (result != 0) {
			return result;
		}

		result = Double.compare(this.getBottom(), comparisonBound.getBottom());
		if (result != 0) {
			return result;
		}

		result = Double.compare(this.getLeft(), comparisonBound.getLeft());
		if (result != 0) {
			return result;
		}

		result = Double.compare(this.getRight(), comparisonBound.getRight());
		if (result != 0) {
			return result;
		}

		String myOrigin = this.getOrigin();
		String otherOrigin = comparisonBound.getOrigin();
		
		// null origin is considered "less" than non-null origin
		if (myOrigin == null) {
			if (otherOrigin == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (otherOrigin == null) {
				return 1;
			} else {
				return myOrigin.compareTo(otherOrigin);
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Bound) {
			return compareTo((Bound) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using both id and version will provide a good distribution of
		 * values but is simple to calculate.
		 */
		return (int) getId() + getVersion();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bound getWriteableInstance() {
		return this;
	}

    /** 
     * ${@inheritDoc}.
     */
    @Override
    public String toString() {
        return "Bound(top=" + getTop() + ", bottom=" + getBottom() + ", left=" + getLeft() + ", right=" + getRight()
				+ ")";
    }
}
