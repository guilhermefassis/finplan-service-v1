import { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';

type SupabaseTokenResponse = {
  access_token: string;
  token_type: string;
  expires_in: number;
  expires_at?: number;
  refresh_token: string;
  user: {
    id: string;
    email: string;
    user_metadata?: { name?: string };
  };
};

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) return null;

        const supabaseUrl = process.env.SUPABASE_URL;
        const supabaseAnonKey = process.env.SUPABASE_ANON_KEY;

        if (!supabaseUrl || !supabaseAnonKey) {
          throw new Error('Missing SUPABASE_URL or SUPABASE_ANON_KEY in env');
        }

        const url = `${supabaseUrl}/auth/v1/token?grant_type=password`;

        const resp = await fetch(url, {
          method: 'POST',
          headers: {
            apikey: supabaseAnonKey,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: credentials.email,
            password: credentials.password,
          }),
        });

        if (!resp.ok) {
          return null;
        }

        const data = (await resp.json()) as SupabaseTokenResponse;
        return {
          id: data.user.id,
          email: data.user.email,
          name: data.user.user_metadata?.name ?? data.user.email,
          supabaseAccessToken: data.access_token,
          supabaseRefreshToken: data.refresh_token,
          supabaseExpiresIn: data.expires_in,
        };
      },
    }),
  ],
  pages: {
    signIn: '/login',
    signOut: '/login',
    error: '/login',
  },
  session: { strategy: 'jwt' },
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.id = (user as any).id;
        token.supabaseAccessToken = (user as any).supabaseAccessToken;
        token.supabaseRefreshToken = (user as any).supabaseRefreshToken;
        token.supabaseExpiresIn = (user as any).supabaseExpiresIn;
      }
      return token;
    },
    async session({ session, token }) {
      if (session?.user) {
        (session.user as any).id = token.id;
        (session as any).supabaseAccessToken = token.supabaseAccessToken;
      }
      return session;
    },
  },
};