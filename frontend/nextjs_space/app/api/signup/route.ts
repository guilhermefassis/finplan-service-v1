import { NextResponse } from 'next/server';
import { mockUsers } from '@/lib/mock-data';
import { PaymentFrequency } from '@/lib/types';

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { email, password, name } = body;

    if (!email || !password || !name) {
      return NextResponse.json(
        { error: 'Missing required fields' },
        { status: 400 }
      );
    }

    // Check if user already exists
    const existingUser = mockUsers.find(u => u.email === email);
    if (existingUser) {
      return NextResponse.json(
        { error: 'User already exists' },
        { status: 409 }
      );
    }

    // Create new user (mock)
    const newUser = {
      id: `550e8400-e29b-41d4-a716-${Date.now()}`,
      email,
      name,
      paymentFrequency: PaymentFrequency.MONTHLY,
      paymentDetails: {},
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    mockUsers.push(newUser);

    return NextResponse.json(
      { message: 'Bloqued', user: { id: newUser.id, email: newUser.email, name: newUser.name } },
      { status: 400 }
    );
  } catch (error) {
    console.error('Signup error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
