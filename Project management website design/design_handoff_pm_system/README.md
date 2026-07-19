# Handoff: Waypoint — Mini Project Management System (Jira-like)

## Overview
A role-based project management app (Admin / Project Manager / Developer) covering dashboards, Kanban board, backlog, sprints, members, worklog reporting, and admin user/project management, per the attached SRS.

## About the Design Files
The bundled file (`Waypoint PM.dc.html`) is a **design reference prototype** built in HTML/React-like syntax (a Claude "Design Component"), not production code to copy directly. It is not Angular and should not be pasted into the Angular app. **Your task is to recreate this design and its interaction behavior as native Angular components**, using the project's existing Angular version, component library (Material/PrimeNG/custom), state approach (services/NgRx/signals), and styling conventions (SCSS, Tailwind, etc.) — whatever is already established in the codebase.

Open the HTML file in a browser to see it live, or ask Claude Code to read it directly — it can parse the file's structure, inline styles, and logic even though it's not Angular syntax.

## Fidelity
**High-fidelity.** Colors, spacing, typography, and copy are final. Recreate pixel-precisely with the codebase's existing Angular UI toolkit rather than introducing a new one — if the app already has Material or another kit, restyle its components to match these values rather than hand-rolling new ones.

## Global Layout
- Two-pane shell: fixed left sidebar (236px) + main content area.
- **Sidebar**: logo (28px rounded-square mark "W" on `#3B4FD9` + "Waypoint" wordmark), project switcher `<select>` (only for non-admin roles, shows `CODE — Name`), vertical nav list, user card pinned at bottom (avatar circle, name, role, "Exit" logout link).
- **Top bar** (58px): current view title (left), today's date (right), bottom border `#E6E8EC`.
- **Content area**: `28px 32px` padding, vertical scroll.
- Base font: Manrope (400/500/600/700/800). Body text ~13-15px. Headings 15-19px, weight 800, tight letter-spacing (-0.01 to -0.02em).
- Card pattern used everywhere: `background:#fff; border:1px solid #E6E8EC; border-radius:12px` (or 8-9px for smaller elements).
- Primary color `#3B4FD9` (buttons, links, active accents), hover `#2F40C4`/`#2A3BB0`.
- Neutral text: headings `#14161C`, body `#4B5160`/`#6B7280`, muted `#9AA1AC`.
- Status colors: To Do `#5B6472`/bg `#EEF0F3`; In Progress `#2A5CD9`/bg `#EAF0FE`; Testing `#B0680A`/bg `#FDF3E3`; Done `#1C8A4B`/bg `#E7F7EE`.
- Priority colors: Low `#5B6472`, Medium `#2A5CD9`, High `#C4720A`, Critical `#D0342C`.
- Buttons: primary = solid `#3B4FD9`/white text/radius 7-8px/700 weight; secondary = white bg, `#E1E4E9` border.
- Toasts: bottom-right fixed, dark `#14161C` bg (success) or `#FDECEC` bg + `#D0342C` text (error), slide-up animation, auto-dismiss ~2.6s.

## Screens / Views
Navigation is a single-page view-switcher (`view` state) inside the shell described above — recreate as Angular routes (e.g. `/dashboard`, `/board`, `/backlog`, `/sprints`, `/sprints/:id`, `/sprints/new`, `/members`, `/worklog`, `/worklog/:userId`, `/admin/users`, `/admin/projects`, `/admin/projects/new`, `/tasks/:id`).

### 1. Login
Centered card (max-width 400px), logo + "Sign in" heading, disabled demo username/password fields, three role buttons: "Continue as Administrator" / "Continue as Project Manager" / "Continue as Developer" — each sets the logged-in role for the demo. In the real app, replace with actual auth; keep the visual card layout.

### 2. Dashboard (role-varying content)
- **Admin**: 4 stat cards (Total Users, Locked Users, Total Projects, Active Projects) in a 4-col grid. (Per latest revision, the "Manage Projects"/"Manage Users" shortcut cards were removed from the dashboard — those actions now live only in the sidebar.)
- **PM**: 4 stat cards (Total Tasks, Logged Hours, Active Sprints, Members); two side-by-side panels — "Tasks by Status" and "Tasks by Priority" horizontal bar breakdowns (label + progress bar + count); a Sprint Progress panel showing active sprint name, days left, progress bar, "done of total" tasks.
- **Developer**: 4 stat cards (My Open Tasks, Completed, Overdue [red if >0], Logged Hours); a "My Open Tasks" list (key, summary, status pill, due date colored red if overdue).

### 3. Projects (list)
Grid of project cards (3-col): project code pill, status pill (Planning/Active/On Hold/Completed), name, description, "N members · N tasks". Admin sees a "+ New Project" button that **navigates to the New Project page** (not a modal/inline form).

### 4. New Project (own page)
Back link "‹ Back to Projects" at top. Card form (max-width 640px): Project Code, Project Name, Description (full width), Start Date, End Date, Create/Cancel buttons. On submit, create project and return to Projects list.

### 5. Board (Kanban)
Two explicit toggle-style buttons at top — **"Showing active sprint only"** and **"Showing all tasks"** (not a single toggle switch): whichever is active gets solid `#3B4FD9` background/white text; the inactive one is white with `#E1E4E9` border. Below, 4 columns (To Do / In Progress / Testing / Done) on a light-grey `#F0F1F3` column background, each with a status-color dot + label + count, and task cards (white, rounded, type label + priority label row, summary, key + assignee avatar). Clicking a card navigates to the Task Detail page.

### 6. Backlog
Filter bar: keyword search, Sprint/Status/Priority/Assignee selects, "+ New Task" button (PM only) toggles an inline creation form (grid form with Summary, Description, Type, Priority, Assignee, Sprint, Story Point, Estimate Hour, Due Date). Table below: Key/Summary/Status/Priority/Assignee/Points/Due columns, row click → Task Detail page. Prev/Next pagination footer.

### 7. Sprints (list)
Card per sprint: name + status pill, Start/Close Sprint buttons (PM only, contextual to status), goal text, date range + task count. "+ New Sprint" button (PM only) **navigates to the New Sprint page**. Clicking a sprint card (not its buttons) navigates to Sprint Detail.

### 8. New Sprint (own page)
Back link "‹ Back to Sprints". Card form (max-width 640px): Sprint Name, Goal, Start Date, End Date, Create/Cancel. Only one sprint per project may be ACTIVE at a time — enforce this rule when starting a sprint (elsewhere in the app).

### 9. Sprint Detail (own page)
Back link "‹ Back to Sprints". Header card: sprint name + status pill, Start/Close Sprint buttons, goal, date range, task/done counts. Below: full task table for that sprint (same columns as Backlog), row click → Task Detail.

### 10. Members
"+ Add Member" (PM only) toggles inline form (user select + project role select + Add/Cancel). Table: avatar+name, project role, status pill, Remove link (PM only, sets member INACTIVE rather than hard delete).

### 11. Worklog Report
Filter bar: Member select, From/To date. Table: User / Total Hours / Number of Tasks, aggregated across worklogs. **Each row is clickable** and navigates to a per-user Worklog Detail page.

### 12. Worklog Detail (own page, new)
Back link "‹ Back to Worklog Report". Header card: user name + total hours. Table of that user's individual worklog entries: Date / Hours / Task key / Description, most recent first.

### 13. Admin — Manage Users
Search box + "+ New User" toggle form (Employee ID, Username, Full Name, Email, Role select). Table: Emp ID / Full Name / Email / Role / Status pill / Edit + Lock-Unlock actions. Edit opens a small modal (this one stays a modal — full Name/Email/Role fields + Save/Cancel). Prev/Next pagination.

### 14. Admin — Manage Projects
Same as Projects screen (#3) but reached via its own sidebar item "Manage Projects"; Admin's "+ New Project" also goes to the New Project page (#4).

### 15. Task Detail (own page, not a dialog)
**Important recent change**: this used to be a modal dialog — it is now a full page with a "‹ Back" link at top that returns to whatever list/board the user came from. Layout: card header with Key + Type + editable Summary (locked for non-owners), 4 tabs (Details / Comments / Worklog / Activity) with underline-indicator active state.
- **Details tab**: 2-col grid — Status (select, options constrained by role: PM = any status; Developer/owner = current + only the next forward status, plus back to TODO from IN_PROGRESS), Priority (PM only), Assignee (PM only), Reporter (read-only), Story Point (PM only), Estimate Hour (owner or PM), Due Date (owner or PM); Description textarea (owner or PM). Shows an inline amber notice when the current user's edit rights are restricted.
- **Comments tab**: avatar + author + timestamp + text list; input + Post button to add a new comment.
- **Worklog tab**: list of date/hours/description/user rows with Edit/Delete for entries the current user owns; form to add a new entry (date, hours 0-24, description).
- **Activity tab**: reverse-chronological audit log (status/priority/assignee changes, comment-added, task-created events) rendered as "User did X" lines with timestamps.

## Interactions & Behavior
- **Permission model**: Project role is per-project (`PM`, `DEV`, `TESTER`) drawn from a project-members join; system role (`ADMIN`/`USER`) is separate and gates the Admin nav items. PM has full edit rights on all task fields in their project; a Developer/Tester can only edit Status (forward-only workflow + back to TODO from IN_PROGRESS), Description, Estimate Hour, and Due Date, and only if they are the task's assignee or reporter. Reassigning a task's assignee is PM-only.
- **Sprint rule**: only one ACTIVE sprint per project at a time; starting a second one is blocked with an error toast.
- All create/update actions that violate a permission or validation rule surface a **toast** (bottom-right, ~2.6s) rather than blocking silently — reuse this pattern for form errors and confirmations app-wide.
- Pagination (Backlog, Admin Users) is simple Prev/Next with a page counter, no jump-to-page.
- Hover states: cards/rows get a subtle border-color or background shift (see `#3B4FD9` border-hover on cards, `#F5F6F8`/`#FAFBFC` background-hover on nav/list rows).

## State Management
Recreate as normal Angular services/store, keyed the same way the prototype's in-memory model is:
- `users`, `projects`, `members` (project↔user join with role + status), `sprints`, `tasks`, `comments`, `worklogs`, `activities` — all flat arrays with foreign-key-style ids, matching a typical relational backend.
- Derive nothing that can be computed: task counts, hours totals, and stat cards are all computed from the above arrays — implement as selectors/computed signals, not stored duplicate state.
- Route/view state (`current view`, `selected task/sprint/user id`, filters, form drafts, pagination page) should map onto Angular Router params/query-params and local component state respectively, replacing the prototype's single `view` string with real navigation.

## Design Tokens
- **Colors**: primary `#3B4FD9` / hover `#2A3BB0`–`#2F40C4`; primary-tint bg `#EAF0FE`; borders `#E6E8EC`/`#E1E4E9`/`#F0F1F3`; text `#14161C` (heading), `#4B5160` (body), `#6B7280` (secondary), `#9AA1AC` (muted); surfaces `#FFFFFF`, `#F5F6F8` (app bg), `#FAFBFC` (subtle row bg); success `#1C8A4B`/`#E7F7EE`; warning `#B0680A`/`#FDF3E3`; danger `#D0342C`/`#FDECEC`.
- **Typography**: Manrope, weights 400/500/600/700/800. Scale roughly: 11px (labels/uppercase eyebrow), 12-13px (body/table), 14-15px (section titles), 17-19px (page/task headings), 22px (login heading), 26px (stat numbers).
- **Radius**: 5px (pills/badges), 7-9px (inputs/small cards/buttons), 12px (panels/cards), 14px (login card, edit-user modal).
- **Spacing**: content padding 28px/32px; card padding 18-20px; form gaps 10-14px; grid gaps 14-16px.
- **Shadows**: login card `0 20px 50px -20px rgba(20,24,40,0.15)`; toast `0 10px 30px -8px rgba(20,24,40,0.25)`.

## Assets
No external images/icons — pure typographic UI with colored pills/dots for status and avatar-initial circles (colored bg `#EAF0FE`, text `#3B4FD9`) for users. No icon font is used; recreate with your app's existing icon set only where explicitly needed (none currently required).

## Screenshots
`screenshots/` contains a full click-through, in order:
1. `01-login.png` — Login
2. `02-admin-dashboard.png` — Admin dashboard
3. `03-admin-projects.png` — Admin: Manage Projects list
4. `04-new-project.png` — New Project page
5. `05-admin-users.png` — Admin: Manage Users
6. `07-pm-dashboard.png` — PM dashboard
7. `08-board.png` — Board (Kanban)
8. `09-backlog.png` — Backlog
9. `10-sprints.png` — Sprints list
10. `11-new-sprint.png` — New Sprint page
11. `12-sprints-list.png` — Sprints list (return)
12. `13-sprint-detail.png` — Sprint Detail page
13. `14-members.png` — Members
14. `15-worklog-report.png` — Worklog Report
15. `16-worklog-detail.png` — Worklog Detail (per-user)
16. `18-task-detail-details.png` — Task Detail, Details tab
17. `19-task-detail-comments.png` — Task Detail, Comments tab
18. `20-task-detail-worklog.png` — Task Detail, Worklog tab
19. `21-task-detail-activity.png` — Task Detail, Activity tab
20. `23-dev-dashboard.png` — Developer dashboard

## Files
- `Waypoint PM.dc.html` — the full interactive prototype (single file, inline styles, React-like logic class). Reference this for exact structure, conditional rendering rules, and computed values; do not import or run it inside the Angular app.
- `screenshots/` — reference captures of every screen (see above).
