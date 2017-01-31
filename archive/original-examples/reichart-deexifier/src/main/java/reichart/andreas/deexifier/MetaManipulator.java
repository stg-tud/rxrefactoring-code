/*******************************************************************************
 * Copyright 2012 Andreas Reichart. Distributed under the terms of the GNU General Public License.
 * 
 * This file is part of DeExifier.
 * 
 * DeExifier is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DeExifier is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeExifier. If not,
 * see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 * 
 */
package reichart.andreas.deexifier;

import java.io.IOException;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;

/**
 * @author Andreas Reichart
 * 
 */
public class MetaManipulator {

    public MetaManipulator() {
	// TODO: MetaManipulator for deleting/keeping special EXIF or IPTC items
    }

    public void remove(byte[] image, boolean removeExif, boolean removeIPTC) {
	try {
	    IImageMetadata meta = Sanselan.getMetadata(image);
	} catch (ImageReadException | IOException e) {
	    e.printStackTrace();
	}
    }

}
