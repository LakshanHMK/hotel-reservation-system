import { Outlet, useLocation } from "react-router";
import Navbar from "./components/Navbar/Navbar.jsx";
import Footer from "./components/Footer/Footer.jsx";
import { isStaticPreview } from "./config/staticPreview.js";
import "./App.css";

function App() {
  const { pathname } = useLocation();
  const mainClassName = pathname === "/"
    ? "main-content main-content--home"
    : "main-content";

  return (
    <div className="app-shell">
      <Navbar />
      {isStaticPreview && (
        <aside className="static-preview-notice" role="note">
          Frontend Preview — Login, reservations and data management require the backend service.
        </aside>
      )}
      <main className={mainClassName}>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

export default App;
