import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Main {
	FastScanner in;
	PrintWriter out;

	class User {
		double avarageRating;
		int numberOfRatings;
		HashMap<Long, Integer> filmRatings;

		User() {
			filmRatings = new HashMap<>();
		}

		void addRating(long filmId, int rating) {
			avarageRating = (avarageRating * numberOfRatings + rating)
					/ (numberOfRatings + 1);
			numberOfRatings++;
			filmRatings.put(filmId, rating);
		}
	}

	class Film {
		double avarageRating;
		int numberOfRatings;
		long id;
		ArrayList<User> allUsers;

		Film(long id) {
			this.id = id;
			allUsers = new ArrayList<>();
		}

		void addRating(int rating, User u) {
			avarageRating = (avarageRating * numberOfRatings + rating)
					/ (numberOfRatings + 1);
			numberOfRatings++;
			allUsers.add(u);
		}
	}

	void solve() throws FileNotFoundException {

		HashMap<Long, User> allUsers = new HashMap<>();
		HashMap<Long, Film> allFilms = new HashMap<>();
	        for (String learnOn : new String[] { "data/train.csv",
			 "data/validation.csv" }) {
			in = new FastScanner(new File(learnOn));
			String header = in.next();
			System.err.println(header);
			while (in.hasMoreTokens()) {
				String[] s = in.next().split(",");
				long userId = Long.parseLong(s[0]);
				long itemId = Long.parseLong(s[1]);
				int rating = Integer.parseInt(s[2]);
				User user = allUsers.get(userId);
				if (user == null) {
					user = new User();
				}
				Film film = allFilms.get(itemId);
				if (film == null) {
					film = new Film(itemId);
				}
				user.addRating(itemId, rating);
				film.addRating(rating, user);
				allUsers.put(userId, user);
				allFilms.put(itemId, film);
			}
		}

		// System.err.println("users cnt = " + allUsers.size());
		// System.err.println("films cnt = " + allFilms.size());

		final String validation = "data/validation.csv";
		final String test = "data/test-ids.csv";

		out = new PrintWriter(new File("submission.csv"));
		out.println("id,rating");
		in = new FastScanner(new File(test));
		in.next();
		while (in.hasMoreTokens()) {
			String[] s = in.next().split(",");
		        int id = Integer.parseInt(s[0]);
			long userId = Long.parseLong(s[1]);
			long itemId = Long.parseLong(s[2]);
//			int rating = Integer.parseInt(s[2]);
			User u = allUsers.get(userId);
			Film f = allFilms.get(itemId);
			double predict = predictRating(f, u, allUsers);
//			sum += (predict - rating) * (predict - rating);
//			cnt++;
			out.println(id+","+predict);
		}
		// System.err.println("RMSE = " + sum / cnt);
		out.close();
	}

	double simularity(User u, User v) {
		if (u.numberOfRatings > v.numberOfRatings) {
			return simularity(v, u);
		}
		double up = 0, s1 = 0, s2 = 0;
		for (Entry<Long, Integer> a : u.filmRatings.entrySet()) {
			Integer vRating = v.filmRatings.get(a.getKey());
			if (vRating == null)
				continue;
			up += (a.getValue() - u.avarageRating)
					* (vRating - v.avarageRating);
			s1 += (a.getValue() - u.avarageRating)
					* (a.getValue() - u.avarageRating);
			s2 += (vRating - v.avarageRating) * (vRating - v.avarageRating);
		}
		if (s1 * s2 == 0) {
			return 0;
		}
		return up / Math.sqrt(s1 * s2);
	}

	double predictRating(Film f, User u, HashMap<Long, User> users) {
		double res = 4;
		if (f == null && u != null) {
			res = (u.avarageRating);
		} else {
			if (f != null && u == null) {
				res = (f.avarageRating);
			} else {
				if (f != null && u != null) {
					if (u.numberOfRatings < 20) {
						res = (Math.sqrt(f.avarageRating * u.avarageRating) - 0.1);
					} else {
						double sumRating = 0, sumSignificance = 0;
						for (User anotherUser : f.allUsers) {
							Integer rating = anotherUser.filmRatings
									.get(f.id);
							if (rating == null)
								continue;
							double sim = simularity(u, anotherUser);
							sumRating += (rating - u.avarageRating) * sim;
							sumSignificance += Math.abs(sim);
						}
						return u.avarageRating + sumRating / sumSignificance;
					}
				}
			}
		}
		if (res < 1)
			res = 1;
		if (res > 5)
			res = 5;
		return res;
	}

	void runIO() throws FileNotFoundException {
		solve();
	}

	class FastScanner {
		BufferedReader br;
		StringTokenizer st;

		public FastScanner(File f) {
			try {
				br = new BufferedReader(new FileReader(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		public FastScanner(InputStream f) {
			br = new BufferedReader(new InputStreamReader(f));
		}

		String next() {
			while (st == null || !st.hasMoreTokens()) {
				String s = null;
				try {
					s = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (s == null)
					return null;
				st = new StringTokenizer(s);
			}
			return st.nextToken();
		}

		boolean hasMoreTokens() {
			while (st == null || !st.hasMoreTokens()) {
				String s = null;
				try {
					s = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (s == null)
					return false;
				st = new StringTokenizer(s);
			}
			return true;
		}

		int nextInt() {
			return Integer.parseInt(next());
		}

		long nextLong() {
			return Long.parseLong(next());
		}

		double nextDouble() {
			return Double.parseDouble(next());
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		new Main().runIO();
	}
}