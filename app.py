import tkinter as tk
from tkinter import ttk, messagebox
import urllib.request
import urllib.error
import json
import urllib.parse

API_BASE_URL = "http://localhost:8080/api"

class StudentManagementApp:
    def __init__(self, root):
        self.root = root
        self.root.title("STUDENT MANAGEMENT SYSTEM")
        self.root.geometry("980x600")
        self.root.minsize(850, 520)
        self.root.configure(bg="#F3F4F6")

        self.setup_styles()
        self.create_header()
        self.create_main_content()
        self.create_status_bar()

        # Initial check & load
        self.check_backend_and_refresh()

    def setup_styles(self):
        style = ttk.Style()
        style.theme_use("clam")

        # Configure colors
        style.configure(".", background="#F3F4F6", font=("Segoe UI", 10))
        
        # Treeview styling
        style.configure(
            "Custom.Treeview",
            background="#FFFFFF",
            foreground="#1F2937",
            rowheight=28,
            fieldbackground="#FFFFFF",
            font=("Segoe UI", 10)
        )
        style.configure(
            "Custom.Treeview.Heading",
            background="#E5E7EB",
            foreground="#111827",
            font=("Segoe UI", 10, "bold"),
            padding=(8, 6)
        )
        style.map("Custom.Treeview", background=[("selected", "#3B82F6")], foreground=[("selected", "#FFFFFF")])

    def create_header(self):
        header_frame = tk.Frame(self.root, bg="#1E293B", padx=20, pady=14)
        header_frame.pack(fill=tk.X, side=tk.TOP)

        title_label = tk.Label(
            header_frame,
            text="STUDENT MANAGEMENT",
            font=("Segoe UI", 16, "bold"),
            fg="#FFFFFF",
            bg="#1E293B"
        )
        title_label.pack(anchor=tk.W)

        subtitle_label = tk.Label(
            header_frame,
            text="A desktop CRUD application powered by Tkinter, Java MVC, JDBC and MySQL",
            font=("Segoe UI", 9),
            fg="#94A3B8",
            bg="#1E293B"
        )
        subtitle_label.pack(anchor=tk.W)

    def create_main_content(self):
        container = tk.Frame(self.root, bg="#F3F4F6", padx=16, pady=16)
        container.pack(fill=tk.BOTH, expand=True)

        # Configure grid columns: left pane for form, right pane for table
        container.grid_columnconfigure(0, weight=0, minsize=320)
        container.grid_columnconfigure(1, weight=1)
        container.grid_rowconfigure(0, weight=1)

        # LEFT PANE: Form
        self.create_form_pane(container)

        # RIGHT PANE: Search + Table
        self.create_table_pane(container)

    def create_form_pane(self, parent):
        form_card = tk.LabelFrame(
            parent,
            text=" Student details ",
            font=("Segoe UI", 11, "bold"),
            bg="#FFFFFF",
            fg="#1E293B",
            padx=16,
            pady=16,
            relief=tk.SOLID,
            bd=1
        )
        form_card.grid(row=0, column=0, sticky="nsew", padx=(0, 12))

        subtext = tk.Label(
            form_card,
            text="Add a new record or select a row to edit it.",
            font=("Segoe UI", 9),
            fg="#64748B",
            bg="#FFFFFF"
        )
        subtext.pack(anchor=tk.W, pady=(0, 14))

        # Registration Number
        reg_label = tk.Label(form_card, text="Registration number", font=("Segoe UI", 9, "bold"), bg="#FFFFFF", fg="#334155")
        reg_label.pack(anchor=tk.W)

        self.reg_entry = ttk.Entry(form_card, font=("Segoe UI", 10))
        self.reg_entry.pack(fill=tk.X, pady=(4, 14))

        # Student Name
        name_label = tk.Label(form_card, text="Student name", font=("Segoe UI", 9, "bold"), bg="#FFFFFF", fg="#334155")
        name_label.pack(anchor=tk.W)

        self.name_entry = ttk.Entry(form_card, font=("Segoe UI", 10))
        self.name_entry.pack(fill=tk.X, pady=(4, 18))

        # Action Buttons
        btn_add = tk.Button(
            form_card,
            text="+ Add student",
            font=("Segoe UI", 10, "bold"),
            bg="#2563EB",
            fg="#FFFFFF",
            activebackground="#1D4ED8",
            activeforeground="#FFFFFF",
            relief=tk.FLAT,
            cursor="hand2",
            pady=6,
            command=self.on_add_student
        )
        btn_add.pack(fill=tk.X, pady=(0, 8))

        btn_update = tk.Button(
            form_card,
            text="✎ Update selected",
            font=("Segoe UI", 10),
            bg="#E0E7FF",
            fg="#3730A3",
            activebackground="#C7D2FE",
            activeforeground="#312E81",
            relief=tk.FLAT,
            cursor="hand2",
            pady=6,
            command=self.on_update_student
        )
        btn_update.pack(fill=tk.X, pady=(0, 8))

        btn_delete = tk.Button(
            form_card,
            text="– Delete selected",
            font=("Segoe UI", 10),
            bg="#FEE2E2",
            fg="#991B1B",
            activebackground="#FECACA",
            activeforeground="#7F1D1D",
            relief=tk.FLAT,
            cursor="hand2",
            pady=6,
            command=self.on_delete_student
        )
        btn_delete.pack(fill=tk.X, pady=(0, 8))

        btn_clear = tk.Button(
            form_card,
            text="Clear form",
            font=("Segoe UI", 10),
            bg="#F1F5F9",
            fg="#475569",
            activebackground="#E2E8F0",
            relief=tk.FLAT,
            cursor="hand2",
            pady=6,
            command=self.clear_form
        )
        btn_clear.pack(fill=tk.X, pady=(0, 16))

        tip_label = tk.Label(
            form_card,
            text="Tip: Select a student in the table to load\ntheir details here.",
            font=("Segoe UI", 8),
            fg="#94A3B8",
            bg="#FFFFFF",
            justify=tk.LEFT
        )
        tip_label.pack(side=tk.BOTTOM, anchor=tk.W)

    def create_table_pane(self, parent):
        table_container = tk.Frame(parent, bg="#F3F4F6")
        table_container.grid(row=0, column=1, sticky="nsew")
        table_container.grid_rowconfigure(1, weight=1)
        table_container.grid_columnconfigure(0, weight=1)

        # Top Bar: Search and Refresh
        search_bar = tk.Frame(table_container, bg="#F3F4F6")
        search_bar.grid(row=0, column=0, sticky="ew", pady=(0, 10))

        self.search_entry = ttk.Entry(search_bar, font=("Segoe UI", 10))
        self.search_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(0, 8))
        self.search_entry.bind("<Return>", lambda event: self.on_search())

        btn_search = tk.Button(
            search_bar,
            text="Search",
            font=("Segoe UI", 9, "bold"),
            bg="#E2E8F0",
            fg="#1E293B",
            relief=tk.GROOVE,
            padx=14,
            pady=4,
            cursor="hand2",
            command=self.on_search
        )
        btn_search.pack(side=tk.LEFT, padx=(0, 6))

        btn_refresh = tk.Button(
            search_bar,
            text="Refresh",
            font=("Segoe UI", 9, "bold"),
            bg="#E2E8F0",
            fg="#1E293B",
            relief=tk.GROOVE,
            padx=14,
            pady=4,
            cursor="hand2",
            command=self.on_refresh
        )
        btn_refresh.pack(side=tk.LEFT)

        # Table Card
        table_card = tk.LabelFrame(
            table_container,
            text=" Student records ",
            font=("Segoe UI", 11, "bold"),
            bg="#FFFFFF",
            fg="#1E293B",
            padx=10,
            pady=10,
            relief=tk.SOLID,
            bd=1
        )
        table_card.grid(row=1, column=0, sticky="nsew")

        # Treeview with scrollbar
        columns = ("registration_number", "student_name")
        self.tree = ttk.Treeview(
            table_card,
            columns=columns,
            show="headings",
            style="Custom.Treeview",
            selectmode="browse"
        )
        self.tree.heading("registration_number", text="REGISTRATION NUMBER", anchor=tk.W)
        self.tree.heading("student_name", text="STUDENT NAME", anchor=tk.W)

        self.tree.column("registration_number", width=180, minwidth=140)
        self.tree.column("student_name", width=340, minwidth=200)

        scrollbar = ttk.Scrollbar(table_card, orient=tk.VERTICAL, command=self.tree.yview)
        self.tree.configure(yscrollcommand=scrollbar.set)

        self.tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

        self.tree.bind("<<TreeviewSelect>>", self.on_row_selected)

    def create_status_bar(self):
        self.status_bar = tk.Frame(self.root, bg="#E2E8F0", height=26, padx=12)
        self.status_bar.pack(fill=tk.X, side=tk.BOTTOM)

        self.status_label = tk.Label(
            self.status_bar,
            text="Connecting to backend...",
            font=("Segoe UI", 9),
            bg="#E2E8F0",
            fg="#475569"
        )
        self.status_label.pack(side=tk.LEFT)

        self.count_label = tk.Label(
            self.status_bar,
            text="Records: 0",
            font=("Segoe UI", 9, "bold"),
            bg="#E2E8F0",
            fg="#475569"
        )
        self.count_label.pack(side=tk.RIGHT)

    # ---------------- API HELPER METHODS ---------------- #

    def api_request(self, endpoint, method="GET", payload=None):
        url = f"{API_BASE_URL}{endpoint}"
        req = urllib.request.Request(url, method=method)
        req.add_header("Content-Type", "application/json; charset=UTF-8")

        data = None
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")

        try:
            with urllib.request.urlopen(req, data=data, timeout=4) as response:
                status_code = response.getcode()
                resp_text = response.read().decode("utf-8")
                try:
                    resp_json = json.loads(resp_text) if resp_text else {}
                except json.JSONDecodeError:
                    resp_json = {}
                return True, resp_json, status_code
        except urllib.error.HTTPError as e:
            resp_text = e.read().decode("utf-8")
            try:
                err_json = json.loads(resp_text)
                err_msg = err_json.get("error", resp_text)
            except Exception:
                err_msg = resp_text or str(e)
            return False, err_msg, e.code
        except urllib.error.URLError as e:
            return False, f"Backend offline or unreachable (Make sure Java server is running on port 8080)", 0
        except Exception as e:
            return False, str(e), 0

    # ---------------- UI HANDLERS & CALLBACKS ---------------- #

    def check_backend_and_refresh(self):
        success, data, code = self.api_request("/health")
        if success and data.get("status") == "UP":
            self.set_status("Connected to Java Backend & MySQL", "#15803D")
            self.load_students()
        else:
            self.set_status(f"Backend offline: {data if isinstance(data, str) else 'Cannot connect to database'}", "#B91C1C")

    def load_students(self, search_query=None):
        endpoint = "/students"
        if search_query:
            endpoint += f"?q={urllib.parse.quote(search_query)}"

        success, data, code = self.api_request(endpoint)
        if success and isinstance(data, list):
            self.tree.delete(*self.tree.get_children())
            for item in data:
                reg_no = item.get("reg_no", "")
                name = item.get("name", "")
                self.tree.insert("", tk.END, values=(reg_no, name))
            self.count_label.config(text=f"Records: {len(data)}")
            if search_query:
                self.set_status(f"Found {len(data)} result(s) for '{search_query}'.", "#1E293B")
            else:
                self.set_status(f"Loaded {len(data)} student record(s).", "#1E293B")
        else:
            self.set_status(f"Failed to load records: {data}", "#B91C1C")

    def on_row_selected(self, event):
        selected_items = self.tree.selection()
        if not selected_items:
            return
        item = selected_items[0]
        values = self.tree.item(item, "values")
        if values and len(values) >= 2:
            self.reg_entry.delete(0, tk.END)
            self.reg_entry.insert(0, values[0])
            self.name_entry.delete(0, tk.END)
            self.name_entry.insert(0, values[1])

    def on_add_student(self):
        reg_no = self.reg_entry.get().strip()
        name = self.name_entry.get().strip()

        if not reg_no or not name:
            messagebox.showwarning("Missing Input", "Please provide both registration number and student name.")
            return

        success, resp, code = self.api_request("/students", method="POST", payload={"reg_no": reg_no, "name": name})
        if success:
            messagebox.showinfo("Success", f"Student {reg_no} successfully added!")
            self.clear_form()
            self.load_students()
        else:
            messagebox.showerror("Error", f"Failed to add student:\n{resp}")

    def on_update_student(self):
        reg_no = self.reg_entry.get().strip()
        name = self.name_entry.get().strip()

        if not reg_no or not name:
            messagebox.showwarning("Missing Input", "Please select a student and provide a new name.")
            return

        endpoint = f"/students/{urllib.parse.quote(reg_no)}"
        success, resp, code = self.api_request(endpoint, method="PUT", payload={"name": name})
        if success:
            messagebox.showinfo("Success", f"Student {reg_no} updated successfully!")
            self.clear_form()
            self.load_students()
        else:
            messagebox.showerror("Error", f"Failed to update student:\n{resp}")

    def on_delete_student(self):
        reg_no = self.reg_entry.get().strip()
        if not reg_no:
            messagebox.showwarning("Selection Required", "Please select or enter the registration number to delete.")
            return

        confirm = messagebox.askyesno(
            "Confirm Delete",
            f"Are you sure you want to delete student with registration number:\n\n{reg_no}?"
        )
        if not confirm:
            return

        endpoint = f"/students/{urllib.parse.quote(reg_no)}"
        success, resp, code = self.api_request(endpoint, method="DELETE")
        if success:
            messagebox.showinfo("Deleted", f"Student {reg_no} deleted successfully.")
            self.clear_form()
            self.load_students()
        else:
            messagebox.showerror("Error", f"Failed to delete student:\n{resp}")

    def on_search(self):
        keyword = self.search_entry.get().strip()
        self.load_students(search_query=keyword)

    def on_refresh(self):
        self.search_entry.delete(0, tk.END)
        self.clear_form()
        self.check_backend_and_refresh()

    def clear_form(self):
        self.reg_entry.delete(0, tk.END)
        self.name_entry.delete(0, tk.END)
        if self.tree.selection():
            self.tree.selection_remove(self.tree.selection())

    def set_status(self, text, color="#475569"):
        self.status_label.config(text=text, fg=color)

if __name__ == "__main__":
    root = tk.Tk()
    app = StudentManagementApp(root)
    root.mainloop()
