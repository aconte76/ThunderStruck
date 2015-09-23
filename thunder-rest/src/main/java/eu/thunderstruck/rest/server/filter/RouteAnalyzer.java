/*==============================================================================
 Copyright (C) 2015. Antonio Conte

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 =============================================================================*/
package eu.thunderstruck.rest.server.filter;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public final class RouteAnalyzer {

	// defines a piece type
	public enum Type {
		PIECE, PARAMETER, WILDCARD
	}

	@Builder
	@ToString
	public static class Piece {
		@Getter
		private Type type;
		@Getter
		private String piece;
	}

	// will not be instantiated
	private RouteAnalyzer() {
	}

	// Return typed List of Pieces
	public static Piece[] getRoutePieces(String route) {
		String[] path = route.split("/", -1);
		List<Piece> pieces = new ArrayList<>();
		for (String part : path) {
			if (part.length() > 0) {
				if (part.startsWith(":")) {
					pieces.add(new Piece(Type.PARAMETER, part.substring(1)));
				} else {
					if (part.equals("*")) {
						pieces.add(new Piece(Type.WILDCARD, part));
					} else {
						pieces.add(new Piece(Type.PIECE, part));
					}
				}
			}
		}
		return pieces.toArray(new Piece[pieces.size()]);
	}

	// Return a wild representation of a route
	public static String getWildcardPatternRoute(String route) {
		if (route.equals("/")) {
			return "/";
		}

		String[] path = route.split("/", -1);
		StringBuilder sb = new StringBuilder(2 * path.length);
		for (String part : path) {
			if (part.length() > 0) {
				sb.append("/");
				sb.append((part.startsWith(":") || part.equals("*")) ? "(.*)" : part);
			}
		}
		return sb.toString();
	}


//	public static boolean isParameter(String routePart) {
//		return routePart.startsWith(":");
//	}
//
//	public static boolean isWildcard(String routePart) {
//		return routePart.equals("*");
//	}
}
