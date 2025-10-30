# Edu-Web Frontend

This is the frontend for the Edu-Web platform, an online learning system. It's built with React, TypeScript, and Vite, using Material-UI for the component library.

## ✨ Features

- User authentication (Login, Signup)
- User profile management (View, Create, Edit)
- Dashboard for students and instructors
- Course enrollment
- Content viewing

## 🛠️ Technologies Used

- **Framework**: [React 19](https://react.dev/)
- **Language**: [TypeScript](https://www.typescriptlang.org/)
- **Build Tool**: [Vite](https://vitejs.dev/)
- **UI Library**: [Material-UI (MUI)](https://mui.com/)
- **Routing**: [React Router DOM](https://reactrouter.com/)
- **State Management**: React Context API
- **Styling**: [Tailwind CSS](https://tailwindcss.com/) & CSS Modules
- **API Communication**: [Axios](https://axios-http.com/)

## 🚀 Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/en/) (v18.x or higher)
- [npm](https://www.npmjs.com/) or [yarn](https://yarnpkg.com/)

### Installation

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    ```
2.  Navigate to the frontend directory:
    ```bash
    cd Edu-Web-FE
    ```
3.  Install the dependencies:
    ```bash
    npm install
    ```

### Environment Variables

Create a `.env.local` file in the root of the `Edu-Web-FE` directory and add the following environment variables. This file will be used to configure the application.

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

This variable points to the backend API gateway. Adjust the URL if your backend is running on a different port.

## 📜 Available Scripts

In the project directory, you can run:

### `npm run dev`

Runs the app in development mode.
Open [http://localhost:5173](http://localhost:5173) to view it in the browser.

The page will reload if you make edits. You will also see any lint errors in the console.

### `npm run build`

Builds the app for production to the `dist` folder.
It correctly bundles React in production mode and optimizes the build for the best performance.

### `npm run lint`

Lints the project files for code quality and style issues.

### `npm run preview`

Runs a local server to preview the production build from the `dist` folder.

